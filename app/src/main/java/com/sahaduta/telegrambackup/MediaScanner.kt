package com.sahaduta.telegrambackup

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

import com.sahaduta.telegrambackup.data.MediaEntity

class MediaScanner(private val context: Context) {

    fun getNewMedia(lastSyncTime: Long): List<MediaEntity> {
        val mediaList = mutableListOf<MediaEntity>()
        
        // Scan Images
        mediaList.addAll(queryMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, lastSyncTime, false))
        
        // Scan Videos
        mediaList.addAll(queryMedia(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, lastSyncTime, true))

        // Sort by oldest first so we backup chronologically
        return mediaList.sortedBy { it.dateAdded }
    }

    private fun queryMedia(collection: Uri, lastSyncTime: Long, isVideo: Boolean): List<MediaEntity> {
        val mediaList = mutableListOf<MediaEntity>()

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE
        )

        // DATE_ADDED is in seconds in MediaStore, so we divide lastSyncTime by 1000 if lastSyncTime is in milliseconds.
        val selection = "${MediaStore.MediaColumns.DATE_ADDED} > ?"
        val selectionArgs = arrayOf((lastSyncTime / 1000).toString())
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
            val bucketColumn = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)

            while (cursor.moveToNext()) {
                if (idColumn == -1) continue
                
                val id = cursor.getLong(idColumn)
                val name = if (nameColumn != -1) cursor.getString(nameColumn) ?: "Unknown" else "Unknown"
                val dateAdded = if (dateAddedColumn != -1) cursor.getLong(dateAddedColumn) else 0L
                val bucketName = if (bucketColumn != -1) cursor.getString(bucketColumn) ?: "Misc" else "Misc"
                val mimeType = if (mimeTypeColumn != -1) cursor.getString(mimeTypeColumn) ?: "*/*" else "*/*"

                val contentUri: Uri = ContentUris.withAppendedId(collection, id)

                mediaList.add(
                    MediaEntity(
                        id = id,
                        uriString = contentUri.toString(),
                        name = name,
                        dateAdded = dateAdded * 1000, // Convert back to milliseconds
                        bucketName = bucketName,
                        mimeType = mimeType,
                        isVideo = isVideo,
                        isBackedUp = false,
                        lastScanned = 0L
                    )
                )
            }
        }
        return mediaList
    }
}
