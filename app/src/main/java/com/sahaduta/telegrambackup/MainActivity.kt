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
import com.sahaduta.telegrambackup.ui.GalleryScreen
import com.sahaduta.telegrambackup.ui.PeopleScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            // If any storage/media permission was granted, start indexing the local gallery immediately
            scheduleIndexerWorker(this)
        }
    }

    private lateinit var telegramManager: TelegramManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestPermissions()
        telegramManager = TelegramManager.getInstance(this)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(telegramManager: TelegramManager) {
    val authState by telegramManager.authState.collectAsState()

    when (authState) {
        AuthState.INITIALIZING -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }
        AuthState.WAIT_PHONE_NUMBER -> PhoneLoginScreen(telegramManager)
        AuthState.WAIT_CODE -> CodeInputScreen(telegramManager)
        AuthState.WAIT_PASSWORD -> PasswordInputScreen(telegramManager)
        AuthState.AUTHENTICATED -> {
            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf("Gallery", "People", "Settings")
            val icons = listOf(
                Icons.Default.PhotoLibrary,
                Icons.Default.Face,
                Icons.Default.Settings
            )

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        tabs.forEachIndexed { index, title ->
                            NavigationBarItem(
                                icon = { Icon(icons[index], contentDescription = title) },
                                label = { Text(title) },
                                selected = selectedTab == index,
                                onClick = { selectedTab = index }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (selectedTab) {
                        0 -> GalleryScreen()
                        1 -> PeopleScreen()
                        2 -> DashboardScreen(telegramManager)
                    }
                }
            }
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
    val isProcessing by telegramManager.isProcessing.collectAsState()
    val errorMessage by telegramManager.errorMessage.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login to Telegram", 
            style = MaterialTheme.typography.headlineMedium, 
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Enter the burner account phone number that will back up your media.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = phoneInput,
            onValueChange = { phoneInput = it },
            label = { Text("Phone Number (with Country Code)") },
            placeholder = { Text("+1234567890") },
            enabled = !isProcessing,
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        if (errorMessage != null) {
            Text(
                text = "Error: $errorMessage",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button(
            onClick = { telegramManager.setPhoneNumber(phoneInput) }, 
            enabled = !isProcessing && phoneInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Send SMS Code")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeInputScreen(telegramManager: TelegramManager) {
    var codeInput by remember { mutableStateOf("") }
    val isProcessing by telegramManager.isProcessing.collectAsState()
    val errorMessage by telegramManager.errorMessage.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter Verification Code", 
            style = MaterialTheme.typography.headlineMedium, 
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "A code was sent via Telegram or SMS to your phone number.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it },
            label = { Text("Auth Code") },
            placeholder = { Text("12345") },
            enabled = !isProcessing,
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        if (errorMessage != null) {
            Text(
                text = "Error: $errorMessage",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button(
            onClick = { telegramManager.checkCode(codeInput) }, 
            enabled = !isProcessing && codeInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Verify Code")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordInputScreen(telegramManager: TelegramManager) {
    var passwordInput by remember { mutableStateOf("") }
    val isProcessing by telegramManager.isProcessing.collectAsState()
    val errorMessage by telegramManager.errorMessage.collectAsState()
    val passwordHint by telegramManager.passwordHint.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Two-Step Verification", 
            style = MaterialTheme.typography.headlineMedium, 
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Your Telegram account is protected by an additional cloud password.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (!passwordHint.isNullOrEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Password Hint:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = passwordHint ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("Cloud Password") },
            enabled = !isProcessing,
            isError = errorMessage != null,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        if (errorMessage != null) {
            Text(
                text = "Error: $errorMessage",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button(
            onClick = { telegramManager.checkPassword(passwordInput) }, 
            enabled = !isProcessing && passwordInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Verify Password")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(telegramManager: TelegramManager) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(context) }
    
    val savedChatId by preferencesManager.chatIdFlow.collectAsState(initial = "")
    var chatIdInput by remember(savedChatId) { mutableStateOf(savedChatId ?: "") }
    
    // Sync states
    val syncStatus by preferencesManager.syncStatusFlow.collectAsState(initial = "Idle")
    val syncProgress by preferencesManager.syncProgressFlow.collectAsState(initial = "")
    val syncError by preferencesManager.syncErrorFlow.collectAsState(initial = "")
    val syncingActive by preferencesManager.syncingActiveFlow.collectAsState(initial = false)
    val lastSyncTime by preferencesManager.lastSyncTimeFlow.collectAsState(initial = 0L)

    // User profile state
    var telegramUser by remember { mutableStateOf<org.drinkless.tdlib.TdApi.User?>(null) }
    
    LaunchedEffect(Unit) {
        val result = telegramManager.getMe()
        if (result.isSuccess) {
            telegramUser = result.getOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // --- Profile Section ---
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = androidx.compose.ui.graphics.Color(0xFF6200EE))
                    }
                    val initial = telegramUser?.firstName?.firstOrNull()?.toString() ?: "?"
                    Text(
                        text = initial,
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (telegramUser != null) "${telegramUser?.firstName} ${telegramUser?.lastName ?: ""}".trim() else "Loading...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (telegramUser != null) "+${telegramUser?.phoneNumber}" else "...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                TextButton(onClick = { telegramManager.logOut() }) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Text(
            text = "Target Configuration",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = chatIdInput,
            onValueChange = { chatIdInput = it },
            label = { Text("Group Chat ID (e.g. -100...)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Sync Interval Setting
        val currentInterval by preferencesManager.syncIntervalFlow.collectAsState(initial = 6)
        Text(
            text = "Auto-Backup Schedule",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start).padding(top = 16.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sync every $currentInterval hours")
            Slider(
                value = currentInterval.toFloat(),
                onValueChange = { scope.launch { preferencesManager.saveSyncInterval(it.toInt()) } },
                valueRange = 1f..24f,
                steps = 23,
                modifier = Modifier.width(150.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    preferencesManager.saveCredentials("", chatIdInput)
                    schedulePeriodicBackup(context, currentInterval)
                    scheduleIndexerWorker(context)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            Text("Save & Enable Auto-Backup")
        }

        // --- Live Sync Status Card ---
        Text(
            text = "Live Sync Status",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (syncingActive) MaterialTheme.colorScheme.primaryContainer 
                               else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = syncStatus,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (syncStatus == "Failed") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    if (syncingActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                }
                
                if (syncProgress.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = syncProgress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (syncError.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = syncError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (!syncingActive && lastSyncTime > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val date = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(lastSyncTime))
                    Text(
                        text = "Last Sync: $date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Button(
            onClick = { triggerImmediateBackup(context) },
            enabled = !syncingActive && chatIdInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (syncingActive) "Syncing..." else "Sync Now")
        }
    }
}

private fun schedulePeriodicBackup(context: android.content.Context, intervalHours: Int = 6) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()
    val periodicWorkRequest = PeriodicWorkRequestBuilder<BackupWorker>(intervalHours.toLong(), TimeUnit.HOURS)
        .setConstraints(constraints).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "TelegramMediaBackupPeriodic", ExistingPeriodicWorkPolicy.UPDATE, periodicWorkRequest
    )
}

private fun scheduleIndexerWorker(context: android.content.Context) {
    val constraints = Constraints.Builder().build()
    // Run indexer immediately
    val oneTimeWorkRequest = OneTimeWorkRequestBuilder<GalleryIndexerWorker>()
        .setConstraints(constraints).build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        "TelegramMediaIndexerManual", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest
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
