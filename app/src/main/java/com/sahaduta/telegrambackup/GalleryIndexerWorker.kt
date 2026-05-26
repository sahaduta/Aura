package com.sahaduta.telegrambackup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.sahaduta.telegrambackup.data.GalleryDatabase
import com.sahaduta.telegrambackup.ml.MLProcessor
import kotlinx.coroutines.flow.firstOrNull

class GalleryIndexerWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val channelId = "indexer_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Gallery ML Indexer", NotificationManager.IMPORTANCE_LOW)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Aura Smart Gallery Indexer")
            .setContentText(progress)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOngoing(true)
            .build()

        return ForegroundInfo(2, notification)
    }

    override suspend fun doWork(): Result {
        val database = GalleryDatabase.getDatabase(context)
        val mlProcessor = MLProcessor(context, database)
        val mediaScanner = MediaScanner(context)
        val preferencesManager = PreferencesManager(context)

        try {
            setForeground(createForegroundInfo("Scanning for new photos..."))

            // 1. Scan for new photos
            var lastSyncTime = preferencesManager.lastSyncTimeFlow.firstOrNull() ?: 0L
            val mediaCount = database.galleryDao().getMediaCount()
            if (mediaCount == 0) {
                lastSyncTime = 0L // Force full scan if DB was wiped
            }
            val newMedia = mediaScanner.getNewMedia(lastSyncTime)
            
            if (newMedia.isNotEmpty()) {
                database.galleryDao().insertMediaList(newMedia)
                val highestDateAdded = newMedia.maxOf { it.dateAdded }
                preferencesManager.saveLastSyncTime(highestDateAdded)
            }

            // 2. Process Unscanned Media (Batch by Batch)
            var unscanned = database.galleryDao().getUnscannedMedia(50)
            var totalProcessed = 0
            
            while (unscanned.isNotEmpty()) {
                for (media in unscanned) {
                    setForeground(createForegroundInfo("Analyzing photo ${totalProcessed + 1}..."))
                    mlProcessor.processMedia(media)
                    totalProcessed++
                }
                // Fetch next batch
                unscanned = database.galleryDao().getUnscannedMedia(50)
            }

            return Result.success()

        } catch (e: Exception) {
            Log.e("GalleryIndexerWorker", "Error indexing gallery", e)
            return Result.failure()
        }
    }
}
