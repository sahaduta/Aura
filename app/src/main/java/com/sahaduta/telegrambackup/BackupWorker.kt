package com.sahaduta.telegrambackup

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.firstOrNull

class BackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val preferencesManager = PreferencesManager(context)
        val telegramManager = TelegramManager(context)
        val mediaScanner = MediaScanner(context)

        // Wait a brief moment to ensure TDLib initializes and checks auth state
        kotlinx.coroutines.delay(2000)

        val authState = telegramManager.authState.firstOrNull()
        if (authState != AuthState.AUTHENTICATED) {
            Log.e("BackupWorker", "TDLib is not authenticated! State: \$authState")
            return Result.failure()
        }

        val chatIdString = preferencesManager.chatIdFlow.firstOrNull()
        if (chatIdString.isNullOrBlank()) {
            Log.e("BackupWorker", "Target Chat ID is missing.")
            return Result.failure()
        }
        val chatId = chatIdString.toLongOrNull() ?: return Result.failure()

        val lastSyncTime = preferencesManager.lastSyncTimeFlow.firstOrNull() ?: 0L
        val newMedia = mediaScanner.getNewMedia(lastSyncTime)

        if (newMedia.isEmpty()) {
            Log.d("BackupWorker", "No new media to backup.")
            return Result.success()
        }

        var highestDateAdded = lastSyncTime

        for (media in newMedia) {
            val folderName = media.bucketName ?: "Misc"
            
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
                    Log.e("BackupWorker", "Failed to create topic for \$folderName", topicResult.exceptionOrNull())
                    // If creating topic fails, it might be because topics aren't enabled.
                    // We can proceed to upload without a topicId.
                }
            }

            // Upload Document via TDLib (Handles up to 2GB natively)
            // Note: input message document requires absolute file path, which MediaStore uri doesn't provide directly.
            // But we can try to use the Uri directly if TDLib supports it, or resolve the true path.
            // Since Android 10+, getting absolute paths is hard. 
            // A robust solution for TDLib is to copy the Uri to a temporary cache file, then pass that path to TDLib.
            
            val tempFile = java.io.File(context.cacheDir, media.name)
            try {
                context.contentResolver.openInputStream(media.uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                val uploadResult = telegramManager.sendDocument(
                    chatId = chatId,
                    threadId = topicId?.toLong() ?: 0L,
                    filePath = tempFile.absolutePath
                )

                if (uploadResult.isSuccess) {
                    if (media.dateAdded > highestDateAdded) {
                        highestDateAdded = media.dateAdded
                        preferencesManager.saveLastSyncTime(highestDateAdded)
                    }
                    Log.d("BackupWorker", "Successfully backed up: \${media.name}")
                } else {
                    Log.e("BackupWorker", "Failed to upload: \${media.name}", uploadResult.exceptionOrNull())
                    return Result.retry()
                }
            } catch (e: Exception) {
                Log.e("BackupWorker", "File copy error: \${media.name}", e)
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete() // Clean up temp file
                }
            }
        }

        return Result.success()
    }
}
