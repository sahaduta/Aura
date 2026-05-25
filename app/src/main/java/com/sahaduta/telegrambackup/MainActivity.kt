package com.sahaduta.telegrambackup

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.work.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions -> }

    private lateinit val telegramManager: TelegramManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestPermissions()
        telegramManager = TelegramManager(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(telegramManager)
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissionLauncher.launch(permissions)
    }
}

@Composable
fun AppContent(telegramManager: TelegramManager) {
    val authState by telegramManager.authState.collectAsState()

    when (authState) {
        AuthState.INITIALIZING -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }
        AuthState.WAIT_PHONE_NUMBER -> {
            PhoneLoginScreen(telegramManager)
        }
        AuthState.WAIT_CODE -> {
            CodeInputScreen(telegramManager)
        }
        AuthState.WAIT_PASSWORD -> {
            PasswordInputScreen(telegramManager)
        }
        AuthState.AUTHENTICATED -> {
            DashboardScreen()
        }
        AuthState.ERROR -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("Error connecting to Telegram.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLoginScreen(telegramManager: TelegramManager) {
    var phoneInput by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login to Telegram (Burner Account)", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 32.dp))
        
        OutlinedTextField(
            value = phoneInput,
            onValueChange = { phoneInput = it },
            label = { Text("Phone Number (with Country Code)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        
        Button(onClick = { telegramManager.setPhoneNumber(phoneInput) }, modifier = Modifier.fillMaxWidth()) {
            Text("Send SMS Code")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeInputScreen(telegramManager: TelegramManager) {
    var codeInput by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter the code from Telegram/SMS", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 32.dp))
        
        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it },
            label = { Text("Auth Code") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        
        Button(onClick = { telegramManager.checkCode(codeInput) }, modifier = Modifier.fillMaxWidth()) {
            Text("Verify")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordInputScreen(telegramManager: TelegramManager) {
    var passwordInput by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter 2-Step Verification Password", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 32.dp))
        
        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        
        Button(onClick = { telegramManager.checkPassword(passwordInput) }, modifier = Modifier.fillMaxWidth()) {
            Text("Verify")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(context) }
    
    val savedChatId by preferencesManager.chatIdFlow.collectAsState(initial = "")
    var chatIdInput by remember(savedChatId) { mutableStateOf(savedChatId ?: "") }
    var isSaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Telegram TDLib Connected!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = chatIdInput,
            onValueChange = { chatIdInput = it; isSaved = false },
            label = { Text("Target Group Chat ID (e.g. -100...)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                scope.launch {
                    preferencesManager.saveCredentials("", chatIdInput) // Bot token no longer needed
                    isSaved = true
                    schedulePeriodicBackup(context)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Save & Enable Auto-Backup (2GB Limit)")
        }

        Button(
            onClick = { triggerImmediateBackup(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sync Now")
        }
        
        if (isSaved) {
            Text(
                text = "Group Chat ID saved and background service scheduled!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

private fun schedulePeriodicBackup(context: android.content.Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()
    val periodicWorkRequest = PeriodicWorkRequestBuilder<BackupWorker>(6, TimeUnit.HOURS)
        .setConstraints(constraints).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "TelegramMediaBackupPeriodic", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest
    )
}

private fun triggerImmediateBackup(context: android.content.Context) {
    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val oneTimeWorkRequest = OneTimeWorkRequestBuilder<BackupWorker>()
        .setConstraints(constraints).build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        "TelegramMediaBackupManual", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest
    )
}
