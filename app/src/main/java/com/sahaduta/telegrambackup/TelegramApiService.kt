package com.sahaduta.telegrambackup

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

data class TelegramResponse<T>(
    val ok: Boolean,
    val result: T?,
    val description: String?
)

data class User(
    val id: Long,
    val is_bot: Boolean,
    val first_name: String,
    val username: String?
)

data class ForumTopic(
    val message_thread_id: Int,
    val name: String,
    val icon_color: Int?
)

data class Message(
    val message_id: Int,
    val message_thread_id: Int?,
    val date: Int
)

interface TelegramApiService {

    @GET("bot{token}/getMe")
    suspend fun getMe(@Path("token") token: String): TelegramResponse<User>

    @FormUrlEncoded
    @POST("bot{token}/createForumTopic")
    suspend fun createForumTopic(
        @Path("token") token: String,
        @Field("chat_id") chatId: String,
        @Field("name") name: String
    ): TelegramResponse<ForumTopic>

    @Multipart
    @POST("bot{token}/sendDocument")
    suspend fun sendDocument(
        @Path("token") token: String,
        @Part("chat_id") chatId: RequestBody,
        @Part("message_thread_id") messageThreadId: RequestBody?,
        @Part document: MultipartBody.Part,
        @Part("caption") caption: RequestBody?
    ): TelegramResponse<Message>
}
