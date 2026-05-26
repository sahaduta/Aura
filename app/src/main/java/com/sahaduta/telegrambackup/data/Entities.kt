package com.sahaduta.telegrambackup.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey val id: Long,
    val uriString: String,
    val name: String,
    val dateAdded: Long,
    val bucketName: String,
    val mimeType: String,
    val isVideo: Boolean,
    val isBackedUp: Boolean = false,
    val lastScanned: Long = 0L // When did ML Kit last process this?
)

@Entity(tableName = "face_clusters")
data class FaceClusterEntity(
    @PrimaryKey(autoGenerate = true) val clusterId: Long = 0,
    val personName: String? = null // Nullable until the user names them
)

@Entity(
    tableName = "face_embeddings",
    foreignKeys = [
        ForeignKey(
            entity = MediaEntity::class,
            parentColumns = ["id"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FaceClusterEntity::class,
            parentColumns = ["clusterId"],
            childColumns = ["clusterId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("mediaId"), Index("clusterId")]
)
data class FaceEmbeddingEntity(
    @PrimaryKey(autoGenerate = true) val embeddingId: Long = 0,
    val mediaId: Long,
    val embeddingData: ByteArray, // The 128D FloatArray converted to bytes
    val clusterId: Long? = null // Assigned when clustered
)

@Entity(
    tableName = "media_tags",
    foreignKeys = [
        ForeignKey(
            entity = MediaEntity::class,
            parentColumns = ["id"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mediaId"), Index("tag")]
)
data class MediaTagEntity(
    @PrimaryKey(autoGenerate = true) val tagId: Long = 0,
    val mediaId: Long,
    val tag: String, // e.g., "Beach", "Car", "Selfie"
    val confidence: Float
)
