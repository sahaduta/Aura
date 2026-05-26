package com.sahaduta.telegrambackup.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GalleryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<MediaEntity>)

    @Query("SELECT * FROM media_items ORDER BY dateAdded DESC")
    fun getAllMediaDesc(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isBackedUp = 0 ORDER BY dateAdded ASC")
    suspend fun getUnbackedUpMedia(): List<MediaEntity>
    
    @Query("SELECT * FROM media_items WHERE lastScanned = 0 ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun getUnscannedMedia(limit: Int = 50): List<MediaEntity>

    @Update
    suspend fun updateMedia(media: MediaEntity)

    // Face Clustering
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaceCluster(cluster: FaceClusterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaceEmbedding(embedding: FaceEmbeddingEntity)

    @Query("SELECT * FROM face_embeddings WHERE clusterId IS NULL")
    suspend fun getUnclusteredEmbeddings(): List<FaceEmbeddingEntity>

    @Query("SELECT * FROM face_embeddings WHERE clusterId = :clusterId")
    suspend fun getEmbeddingsForCluster(clusterId: Long): List<FaceEmbeddingEntity>

    @Query("SELECT * FROM face_clusters")
    fun getAllClusters(): Flow<List<FaceClusterEntity>>

    @Query("SELECT * FROM face_clusters")
    suspend fun getAllClustersSync(): List<FaceClusterEntity>

    @Update
    suspend fun updateFaceCluster(cluster: FaceClusterEntity)

    @Query("UPDATE face_embeddings SET clusterId = :clusterId WHERE embeddingId = :embeddingId")
    suspend fun assignEmbeddingToCluster(embeddingId: Long, clusterId: Long)

    // Tags
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaTag(tag: MediaTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaTags(tags: List<MediaTagEntity>)

    @Query("SELECT * FROM media_tags WHERE mediaId = :mediaId")
    suspend fun getTagsForMedia(mediaId: Long): List<MediaTagEntity>
    
    // Search
    @Query("""
        SELECT DISTINCT m.* FROM media_items m
        LEFT JOIN media_tags t ON m.id = t.mediaId
        LEFT JOIN face_embeddings e ON m.id = e.mediaId
        LEFT JOIN face_clusters c ON e.clusterId = c.clusterId
        WHERE 
            m.name LIKE '%' || :query || '%' OR 
            t.tag LIKE '%' || :query || '%' OR 
            c.personName LIKE '%' || :query || '%'
        ORDER BY m.dateAdded DESC
    """)
    fun searchMedia(query: String): Flow<List<MediaEntity>>
}
