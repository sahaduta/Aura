package com.sahaduta.telegrambackup

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class BackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val preferencesManager = PreferencesManager(context)
        val telegramManager = TelegramManager.getInstance(context)
        val mediaScanner = MediaScanner(context)

        preferencesManager.saveSyncingActive(true)
        preferencesManager.saveSyncStatus("Initializing TDLib...")
        preferencesManager.saveSyncError("")

        // Wait for TDLib auth state to resolve past INITIALIZING
        val authState = withTimeoutOrNull(10000) {
            telegramManager.authState.first { it != AuthState.INITIALIZING }
        } ?: AuthState.INITIALIZING

        if (authState != AuthState.AUTHENTICATED) {
            Log.e("BackupWorker", "TDLib is not authenticated! State: $authState")
            preferencesManager.saveSyncStatus("Failed")
            preferencesManager.saveSyncError("Telegram not logged in!")
            preferencesManager.saveSyncingActive(false)
            return Result.failure()
        }

        val chatIdString = preferencesManager.chatIdFlow.firstOrNull()
        if (chatIdString.isNullOrBlank()) {
            Log.e("BackupWorker", "Target Chat ID is missing.")
            preferencesManager.saveSyncStatus("Failed")
            preferencesManager.saveSyncError("Target Group Chat ID is missing.")
            preferencesManager.saveSyncingActive(false)
            return Result.failure()
        }
        val chatId = chatIdString.toLongOrNull() ?: return Result.failure()

        preferencesManager.saveSyncStatus("Scanning media...")
        val lastSyncTime = preferencesManager.lastSyncTimeFlow.firstOrNull() ?: 0L
        val newMedia = mediaScanner.getNewMedia(lastSyncTime)

        if (newMedia.isEmpty()) {
            Log.d("BackupWorker", "No new media to backup.")
            preferencesManager.saveSyncStatus("Idle")
            preferencesManager.saveSyncProgress("All media is backed up.")
            preferencesManager.saveSyncingActive(false)
            return Result.success()
        }

        preferencesManager.saveSyncStatus("Uploading...")
        var highestDateAdded = lastSyncTime
        val totalFiles = newMedia.size

        for ((index, media) in newMedia.withIndex()) {
            val progressText = "Uploading file ${index + 1} of $totalFiles: ${media.name}"
            preferencesManager.saveSyncProgress(progressText)
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
                    Log.e("BackupWorker", "Failed to create topic for $folderName", topicResult.exceptionOrNull())
                }
            }

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
                    Log.d("BackupWorker", "Successfully backed up: ${media.name}")
                } else {
                    Log.e("BackupWorker", "Failed to upload: ${media.name}", uploadResult.exceptionOrNull())
                    preferencesManager.saveSyncStatus("Failed")
                    preferencesManager.saveSyncError("Failed to upload: ${media.name}")
                    preferencesManager.saveSyncingActive(false)
                    return Result.retry()
                }
            } catch (e: Exception) {
                Log.e("BackupWorker", "File copy error: ${media.name}", e)
                preferencesManager.saveSyncStatus("Failed")
                preferencesManager.saveSyncError("File copy error: ${e.message}")
                preferencesManager.saveSyncingActive(false)
                return Result.retry()
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete() // Clean up temp file
                }
            }
        }

        preferencesManager.saveSyncStatus("Success")
        preferencesManager.saveSyncProgress("All files backed up successfully!")
        preferencesManager.saveSyncingActive(false)
        return Result.success()
    }
}
