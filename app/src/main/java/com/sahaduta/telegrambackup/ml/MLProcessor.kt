package com.sahaduta.telegrambackup.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.sahaduta.telegrambackup.data.*
import kotlinx.coroutines.tasks.await
import java.nio.ByteBuffer

class MLProcessor(private val context: Context, private val database: GalleryDatabase) {

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f)
            .build()
    )

    private val imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    
    private val embeddingModel = FaceEmbeddingModel(context)

    // Threshold for Cosine Similarity (Higher means stricter match)
    private val FACE_MATCH_THRESHOLD = 0.7f

    suspend fun processMedia(mediaEntity: MediaEntity) {
        try {
            val bitmap = loadBitmap(Uri.parse(mediaEntity.uriString))
            if (bitmap == null) {
                markAsScanned(mediaEntity)
                return
            }

            val image = InputImage.fromBitmap(bitmap, 0)

            // 1. Image Labeling
            val labels = imageLabeler.process(image).await()
            val tagEntities = labels.map { label ->
                MediaTagEntity(
                    mediaId = mediaEntity.id,
                    tag = label.text,
                    confidence = label.confidence
                )
            }
            if (tagEntities.isNotEmpty()) {
                database.galleryDao().insertMediaTags(tagEntities)
            }

            // 2. Face Detection & Embedding
            val faces = faceDetector.process(image).await()
            for (face in faces) {
                val boundingBox = face.boundingBox
                
                // Ensure bounding box is within bitmap bounds
                val x = Math.max(0, boundingBox.left)
                val y = Math.max(0, boundingBox.top)
                val width = Math.min(bitmap.width - x, boundingBox.width())
                val height = Math.min(bitmap.height - y, boundingBox.height())
                
                if (width <= 0 || height <= 0) continue

                val faceCrop = Bitmap.createBitmap(bitmap, x, y, width, height)
                val embedding = embeddingModel.getFaceEmbedding(faceCrop)
                val embeddingBytes = floatArrayToByteArray(embedding)

                val embeddingEntity = FaceEmbeddingEntity(
                    mediaId = mediaEntity.id,
                    embeddingData = embeddingBytes,
                    clusterId = null // Will be assigned in clustering step
                )
                database.galleryDao().insertFaceEmbedding(embeddingEntity)
            }

            markAsScanned(mediaEntity)
            
            // 3. Cluster new embeddings
            clusterNewEmbeddings()

        } catch (e: Exception) {
            Log.e("MLProcessor", "Error processing media ${mediaEntity.name}", e)
            markAsScanned(mediaEntity) // Mark as scanned even if it failed so we don't infinitely retry
        }
    }

    private suspend fun markAsScanned(media: MediaEntity) {
        val updated = media.copy(lastScanned = System.currentTimeMillis())
        database.galleryDao().updateMedia(updated)
    }

    private suspend fun clusterNewEmbeddings() {
        val unclustered = database.galleryDao().getUnclusteredEmbeddings()
        val existingClusters = database.galleryDao().getAllClustersSync()
        
        // Cache cluster centroids for performance (average embedding of the cluster)
        // For simplicity in this demo, we'll just compare against all embeddings in the database
        // In a production app, you'd calculate a centroid for each cluster.
        
        for (newEmbedding in unclustered) {
            val newVector = byteArrayToFloatArray(newEmbedding.embeddingData)
            var bestMatchClusterId: Long? = null
            var bestSimilarity = 0f

            for (cluster in existingClusters) {
                // Get all embeddings for this cluster to find the closest match
                val clusterEmbeddings = database.galleryDao().getEmbeddingsForCluster(cluster.clusterId)
                for (clusterEmb in clusterEmbeddings) {
                    val vector = byteArrayToFloatArray(clusterEmb.embeddingData)
                    val sim = cosineSimilarity(newVector, vector)
                    if (sim > bestSimilarity && sim >= FACE_MATCH_THRESHOLD) {
                        bestSimilarity = sim
                        bestMatchClusterId = cluster.clusterId
                    }
                }
            }

            if (bestMatchClusterId != null) {
                // Found a match!
                database.galleryDao().assignEmbeddingToCluster(newEmbedding.embeddingId, bestMatchClusterId)
            } else {
                // Create a new cluster
                val newCluster = FaceClusterEntity()
                val newClusterId = database.galleryDao().insertFaceCluster(newCluster)
                database.galleryDao().assignEmbeddingToCluster(newEmbedding.embeddingId, newClusterId)
                // Add the new cluster to our running list
                existingClusters.toMutableList().add(newCluster.copy(clusterId = newClusterId))
            }
        }
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                // Downsample large images for memory safety
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                    if (info.size.width > 2048 || info.size.height > 2048) {
                        decoder.setTargetSampleSize(2)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            Log.e("MLProcessor", "Failed to load bitmap", e)
            null
        }
    }

    private fun cosineSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in vectorA.indices) {
            dotProduct += vectorA[i] * vectorB[i]
            normA += vectorA[i] * vectorA[i]
            normB += vectorB[i] * vectorB[i]
        }
        if (normA == 0f || normB == 0f) return 0f
        return dotProduct / (Math.sqrt(normA.toDouble()) * Math.sqrt(normB.toDouble())).toFloat()
    }

    private fun floatArrayToByteArray(floats: FloatArray): ByteArray {
        val buffer = ByteBuffer.allocate(floats.size * 4)
        for (f in floats) {
            buffer.putFloat(f)
        }
        return buffer.array()
    }

    private fun byteArrayToFloatArray(bytes: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(bytes)
        val floats = FloatArray(bytes.size / 4)
        for (i in floats.indices) {
            floats[i] = buffer.float
        }
        return floats
    }
}
