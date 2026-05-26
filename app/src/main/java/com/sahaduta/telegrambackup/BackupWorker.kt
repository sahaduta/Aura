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
import android.content.pm.ServiceInfo
import com.sahaduta.telegrambackup.data.GalleryDatabase
import com.sahaduta.telegrambackup.data.MediaEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class BackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val channelId = "backup_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Backup Progress", NotificationManager.IMPORTANCE_LOW)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Telegram Media Backup")
            .setContentText(progress)
            .setSmallIcon(android.R.drawable.ic_popup_sync) // default system sync icon
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(1, notification)
        }
    }

    override suspend fun doWork(): Result {
        val preferencesManager = PreferencesManager(context)
        
        try {
            setForeground(createForegroundInfo("Starting backup..."))
            val database = GalleryDatabase.getDatabase(context)
            val telegramManager = TelegramManager.getInstance(context)

            preferencesManager.saveSyncingActive(true)
            preferencesManager.saveSyncStatus("Initializing TDLib...")
            preferencesManager.saveSyncError("")
            preferencesManager.saveSyncProgress("")

            // Wait for TDLib auth state to resolve past INITIALIZING (up to 15 seconds)
            val authState = withTimeoutOrNull(15000) {
                telegramManager.authState.first { it != AuthState.INITIALIZING }
            } ?: AuthState.INITIALIZING

            if (authState != AuthState.AUTHENTICATED) {
                Log.e("BackupWorker", "TDLib is not authenticated! State: $authState")
                preferencesManager.saveSyncStatus("Failed")
                preferencesManager.saveSyncError("Telegram not logged in! Please open the app and log in first.")
                preferencesManager.saveSyncingActive(false)
                return Result.failure()
            }

            val chatIdString = preferencesManager.chatIdFlow.firstOrNull()
            if (chatIdString.isNullOrBlank()) {
                Log.e("BackupWorker", "Target Chat ID is missing.")
                preferencesManager.saveSyncStatus("Failed")
                preferencesManager.saveSyncError("Target Group Chat ID is missing. Please set it in the app.")
                preferencesManager.saveSyncingActive(false)
                return Result.failure()
            }
            val chatId = chatIdString.toLongOrNull()
            if (chatId == null) {
                Log.e("BackupWorker", "Invalid Chat ID format: $chatIdString")
                preferencesManager.saveSyncStatus("Failed")
                preferencesManager.saveSyncError("Invalid Chat ID format: '$chatIdString'. Must be a number like -100...")
                preferencesManager.saveSyncingActive(false)
                return Result.failure()
            }

            preferencesManager.saveSyncStatus("Scanning media...")
            val newMedia = database.galleryDao().getUnbackedUpMedia()

            if (newMedia.isEmpty()) {
                Log.d("BackupWorker", "No new media to backup.")
                preferencesManager.saveSyncStatus("Idle")
                preferencesManager.saveSyncProgress("All media is backed up.")
                preferencesManager.saveSyncingActive(false)
                return Result.success()
            }

            preferencesManager.saveSyncStatus("Uploading...")
            val totalFiles = newMedia.size
            var successCount = 0

            for ((index, media) in newMedia.withIndex()) {
                val progressText = "Uploading file ${index + 1} of $totalFiles: ${media.name}"
                Log.d("BackupWorker", progressText)
                preferencesManager.saveSyncProgress(progressText)
                setForeground(createForegroundInfo(progressText))
                val folderName = media.bucketName

                // Fetch Tags and Faces for this media to create Caption
                val tags = database.galleryDao().getTagsForMedia(media.id)
                // We'll create a caption like #Scenery #FaceClusterName
                val captionBuilder = StringBuilder()
                for (tag in tags) {
                    val cleanTag = tag.tag.replace(" ", "")
                    captionBuilder.append("#$cleanTag ")
                }
                
                // Fetch Faces
                val embeddings = database.galleryDao().getUnclusteredEmbeddings() // Wait, need a direct query. Let's do a raw lookup for now or just skip specific faces in caption until they are named.
                // For now, let's just use ML Kit labels as hashtags.

                val captionText = captionBuilder.toString().trim()
                
                // Get or Create Topic
                var topicId = preferencesManager.getTopicIdFlow(folderName).firstOrNull()
                
                if (topicId == null || topicId == 0) {
                    val topicResult = telegramManager.createForumTopic(chatId, folderName)
                    if (topicResult.isSuccess) {
                        val newTopicId = topicResult.getOrNull()
                        if (newTopicId != null) {
                            topicId = newTopicId.toInt()
                            preferencesManager.saveTopicId(folderName, topicId)
                        }
                    } else {
                        Log.e("BackupWorker", "Failed to create topic for $folderName: ${topicResult.exceptionOrNull()?.message}")
                        // If forum topics aren't enabled on the group, proceed without topicId
                    }
                }

                val tempFile = java.io.File(context.cacheDir, "backup_${System.currentTimeMillis()}_${media.name}")
                try {
                    // Copy from MediaStore URI to temp file
                    val contentUri = android.net.Uri.parse(media.uriString)
                    val inputStream = context.contentResolver.openInputStream(contentUri)
                    if (inputStream == null) {
                        Log.w("BackupWorker", "Skipping ${media.name}: could not open input stream (file may have been deleted)")
                        continue
                    }
                    inputStream.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Verify the temp file is not empty
                    if (tempFile.length() == 0L) {
                        Log.w("BackupWorker", "Skipping ${media.name}: temp file is empty")
                        continue
                    }
                    
                    val uploadResult = telegramManager.sendDocumentAndWait(
                        chatId = chatId,
                        threadId = topicId?.toLong() ?: 0L,
                        filePath = tempFile.absolutePath,
                        caption = captionText
                    )

                    if (uploadResult.isSuccess) {
                        successCount++
                        // Mark as backed up
                        database.galleryDao().updateMedia(media.copy(isBackedUp = true))
                        Log.d("BackupWorker", "Successfully backed up: ${media.name}")
                    } else {
                        val errorMsg = uploadResult.exceptionOrNull()?.message ?: "Unknown error"
                        Log.e("BackupWorker", "Failed to upload: ${media.name} - $errorMsg")
                        preferencesManager.saveSyncStatus("Failed")
                        preferencesManager.saveSyncError("Failed to upload ${media.name}: $errorMsg")
                        preferencesManager.saveSyncingActive(false)
                        return Result.retry()
                    }
                } catch (e: Exception) {
                    Log.e("BackupWorker", "Error processing ${media.name}", e)
                    preferencesManager.saveSyncStatus("Failed")
                    preferencesManager.saveSyncError("Error processing ${media.name}: ${e.message}")
                    preferencesManager.saveSyncingActive(false)
                    return Result.retry()
                } finally {
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                }
            }

            preferencesManager.saveSyncStatus("Success")
            preferencesManager.saveSyncProgress("Backed up $successCount of $totalFiles files successfully!")
            preferencesManager.saveSyncingActive(false)
            return Result.success()
            
        } catch (e: Exception) {
            Log.e("BackupWorker", "Fatal error in worker", e)
            preferencesManager.saveSyncStatus("Failed")
            preferencesManager.saveSyncError("Fatal error: ${e.javaClass.simpleName}: ${e.message}")
            preferencesManager.saveSyncingActive(false)
            return Result.failure()
        }
    }
}
