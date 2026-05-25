package com.sahaduta.telegrambackup

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateAdded: Long,
    val bucketName: String?,
    val mimeType: String,
    val isVideo: Boolean
)

class MediaScanner(private val context: Context) {

    fun getNewMedia(lastSyncTime: Long): List<MediaItem> {
        val mediaList = mutableListOf<MediaItem>()
        
        // Scan Images
        mediaList.addAll(queryMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, lastSyncTime, false))
        
        // Scan Videos
        mediaList.addAll(queryMedia(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, lastSyncTime, true))

        // Sort by oldest first so we backup chronologically
        return mediaList.sortedBy { it.dateAdded }
    }

    private fun queryMedia(collection: Uri, lastSyncTime: Long, isVideo: Boolean): List<MediaItem> {
        val mediaList = mutableListOf<MediaItem>()

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE
        )

        // DATE_ADDED is in seconds in MediaStore, so we divide lastSyncTime by 1000 if lastSyncTime is in milliseconds.
        val selection = "\${MediaStore.MediaColumns.DATE_ADDED} > ?"
        val selectionArgs = arrayOf((lastSyncTime / 1000).toString())
        val sortOrder = "\${MediaStore.MediaColumns.DATE_ADDED} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val dateAdded = cursor.getLong(dateAddedColumn)
                val bucketName = cursor.getString(bucketColumn) ?: "Misc"
                val mimeType = cursor.getString(mimeTypeColumn) ?: "*/*"

                val contentUri: Uri = ContentUris.withAppendedId(collection, id)

                mediaList.add(
                    MediaItem(
                        id = id,
                        uri = contentUri,
                        name = name,
                        dateAdded = dateAdded * 1000, // Convert back to milliseconds
                        bucketName = bucketName,
                        mimeType = mimeType,
                        isVideo = isVideo
                    )
                )
            }
        }
        return mediaList
    }
}
