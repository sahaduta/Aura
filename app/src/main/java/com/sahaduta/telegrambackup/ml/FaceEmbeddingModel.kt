package com.sahaduta.telegrambackup.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FaceEmbeddingModel(context: Context) {

    private val interpreter: Interpreter

    // MobileFaceNet parameters
    private val inputImageSize = 112
    private val embeddingSize = 192 // MobileFaceNet output is usually 192

    init {
        // Load the model from assets
        val modelBuffer = FileUtil.loadMappedFile(context, "mobile_face_net.tflite")
        val options = Interpreter.Options()
        // Use 4 threads for better performance on CPU
        options.setNumThreads(4)
        interpreter = Interpreter(modelBuffer, options)
    }

    /**
     * Takes a cropped face bitmap, resizes it, normalizes it, and returns the embedding float array.
     */
    fun getFaceEmbedding(faceCrop: Bitmap): FloatArray {
        // 1. Resize the face crop to 112x112
        val resizedBitmap = Bitmap.createScaledBitmap(faceCrop, inputImageSize, inputImageSize, true)

        // 2. Convert Bitmap to ByteBuffer
        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // 3. Run Inference
        val outputBuffer = Array(1) { FloatArray(embeddingSize) }
        interpreter.run(byteBuffer, outputBuffer)

        // 4. Return normalized embedding
        return normalize(outputBuffer[0])
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // Batch size 1, Height, Width, Channels (RGB)
        val byteBuffer = ByteBuffer.allocateDirect(1 * inputImageSize * inputImageSize * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputImageSize * inputImageSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until inputImageSize) {
            for (j in 0 until inputImageSize) {
                val value = intValues[pixel++]
                // MobileFaceNet typically expects normalized inputs [-1, 1]
                byteBuffer.putFloat(((value shr 16 and 0xFF) - 127.5f) / 128.0f) // R
                byteBuffer.putFloat(((value shr 8 and 0xFF) - 127.5f) / 128.0f)  // G
                byteBuffer.putFloat(((value and 0xFF) - 127.5f) / 128.0f)       // B
            }
        }
        return byteBuffer
    }

    private fun normalize(embedding: FloatArray): FloatArray {
        var sum = 0f
        for (v in embedding) {
            sum += v * v
        }
        val norm = Math.sqrt(sum.toDouble()).toFloat()
        val normalized = FloatArray(embeddingSize)
        for (i in embedding.indices) {
            normalized[i] = embedding[i] / norm
        }
        return normalized
    }
    
    fun close() {
        interpreter.close()
    }
}
