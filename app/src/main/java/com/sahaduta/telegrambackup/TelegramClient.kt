package com.sahaduta.telegrambackup

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class TelegramClient(private val context: Context) {

    private val apiService: TelegramApiService

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.telegram.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(TelegramApiService::class.java)
    }

    suspend fun getMe(token: String): Result<User> {
        return try {
            val response = apiService.getMe(token)
            if (response.ok && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.description ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createForumTopic(token: String, chatId: String, name: String): Result<Int> {
        return try {
            val response = apiService.createForumTopic(token, chatId, name)
            if (response.ok && response.result != null) {
                Result.success(response.result.message_thread_id)
            } else {
                Result.failure(Exception(response.description ?: "Failed to create topic"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendDocument(
        token: String,
        chatId: String,
        messageThreadId: Int?,
        uri: Uri,
        mimeType: String,
        fileName: String
    ): Result<Boolean> {
        return try {
            val chatIdBody = chatId.toRequestBody("text/plain".toMediaTypeOrNull())
            val threadIdBody = messageThreadId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val requestBody = object : RequestBody() {
                override fun contentType() = mimeType.toMediaTypeOrNull()
                override fun writeTo(sink: BufferedSink) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.source().use { source ->
                            sink.writeAll(source)
                        }
                    }
                }
            }

            val documentPart = MultipartBody.Part.createFormData("document", fileName, requestBody)

            val response = apiService.sendDocument(token, chatIdBody, threadIdBody, documentPart, null)
            if (response.ok) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.description ?: "Failed to upload document"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
