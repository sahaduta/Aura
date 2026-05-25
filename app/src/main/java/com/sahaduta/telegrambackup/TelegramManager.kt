package com.sahaduta.telegrambackup

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class AuthState {
    INITIALIZING,
    WAIT_PHONE_NUMBER,
    WAIT_CODE,
    WAIT_PASSWORD,
    AUTHENTICATED,
    ERROR
}

class TelegramManager(private val context: Context) : Client.ResultHandler {

    private var client: Client? = null
    
    private val _authState = MutableStateFlow(AuthState.INITIALIZING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _passwordHint = MutableStateFlow<String?>(null)
    val passwordHint: StateFlow<String?> = _passwordHint.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Replace these with your own from my.telegram.org
    private val API_ID = 94575
    private val API_HASH = "a3406de8d171bb422bb6ddf3bbd800e2"

    init {
        // Create the TDLib client
        client = Client.create(this, null, null)
    }

    override fun onResult(obj: TdApi.Object) {
        when (obj.constructor) {
            TdApi.UpdateAuthorizationState.CONSTRUCTOR -> {
                val authStateObj = (obj as TdApi.UpdateAuthorizationState).authorizationState
                handleAuthState(authStateObj)
            }
            else -> {
                // Handle other updates if needed
                Log.d("TelegramManager", "Received: \${obj.javaClass.simpleName}")
            }
        }
    }

    private fun handleAuthState(state: TdApi.AuthorizationState) {
        when (state.constructor) {
            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                val parameters = TdApi.SetTdlibParameters()
                parameters.databaseDirectory = File(context.filesDir, "tdlib").absolutePath
                parameters.useMessageDatabase = true
                parameters.useSecretChats = true
                parameters.apiId = API_ID
                parameters.apiHash = API_HASH
                parameters.systemLanguageCode = "en"
                parameters.deviceModel = "Android"
                parameters.applicationVersion = "1.0"
                parameters.databaseEncryptionKey = ByteArray(0)
                
                client?.send(parameters, this)
            }
            TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                _authState.value = AuthState.WAIT_PHONE_NUMBER
                _isProcessing.value = false
            }
            TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                _authState.value = AuthState.WAIT_CODE
                _isProcessing.value = false
            }
            TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                val waitPasswordState = state as TdApi.AuthorizationStateWaitPassword
                _passwordHint.value = waitPasswordState.passwordHint.takeIf { it.isNotEmpty() }
                _authState.value = AuthState.WAIT_PASSWORD
                _isProcessing.value = false
            }
            TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                _authState.value = AuthState.AUTHENTICATED
                _isProcessing.value = false
            }
            TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                Log.i("TelegramManager", "TDLib connection closed")
                _isProcessing.value = false
            }
            else -> {
                Log.d("TelegramManager", "Unhandled auth state: \${state.javaClass.simpleName}")
            }
        }
    }

    fun setPhoneNumber(phoneNumber: String) {
        _isProcessing.value = true
        _errorMessage.value = null
        client?.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) { result ->
            _isProcessing.value = false
            if (result is TdApi.Error) {
                Log.e("TelegramManager", "Set phone number failed: ${result.message}")
                _errorMessage.value = result.message
            }
        }
    }

    fun checkCode(code: String) {
        _isProcessing.value = true
        _errorMessage.value = null
        client?.send(TdApi.CheckAuthenticationCode(code)) { result ->
            _isProcessing.value = false
            if (result is TdApi.Error) {
                Log.e("TelegramManager", "Check code failed: ${result.message}")
                _errorMessage.value = result.message
            }
        }
    }

    fun checkPassword(password: String) {
        _isProcessing.value = true
        _errorMessage.value = null
        client?.send(TdApi.CheckAuthenticationPassword(password)) { result ->
            _isProcessing.value = false
            if (result is TdApi.Error) {
                Log.e("TelegramManager", "Check password failed: ${result.message}")
                _errorMessage.value = result.message
            }
        }
    }

    suspend fun createForumTopic(chatId: Long, name: String): Result<Long> = suspendCoroutine { cont ->
        val request = TdApi.CreateForumTopic(chatId, name, false, TdApi.ForumTopicIcon())
        client?.send(request) { result ->
            if (result is TdApi.ForumTopicInfo) {
                cont.resume(Result.success(result.forumTopicId.toLong()))
            } else if (result is TdApi.Error) {
                cont.resume(Result.failure(Exception(result.message)))
            } else {
                cont.resume(Result.failure(Exception("Unknown response")))
            }
        }
    }

    suspend fun sendDocument(chatId: Long, threadId: Long, filePath: String): Result<Boolean> = suspendCoroutine { cont ->
        val document = TdApi.InputMessageDocument(
            TdApi.InputFileLocal(filePath), 
            null, 
            true, // disableContentTypeDetection
            null
        )
        
        val request = TdApi.SendMessage(
            chatId,
            if (threadId > 0) TdApi.MessageTopicForum(threadId.toInt()) else null,
            null,
            null,
            null,
            document
        )

        client?.send(request) { result ->
            if (result is TdApi.Message) {
                cont.resume(Result.success(true))
            } else if (result is TdApi.Error) {
                cont.resume(Result.failure(Exception(result.message)))
            } else {
                cont.resume(Result.failure(Exception("Unknown error")))
            }
        }
    }
}
