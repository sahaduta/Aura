package com.sahaduta.telegrambackup

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val BOT_TOKEN = stringPreferencesKey("bot_token")
        val CHAT_ID = stringPreferencesKey("chat_id")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val SYNC_STATUS = stringPreferencesKey("sync_status")
        val SYNC_PROGRESS = stringPreferencesKey("sync_progress")
        val SYNC_ERROR = stringPreferencesKey("sync_error")
        val SYNCING_ACTIVE = booleanPreferencesKey("syncing_active")
        val SYNC_INTERVAL = intPreferencesKey("sync_interval_hours")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
    }

    val botTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[BOT_TOKEN]
    }

    val chatIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CHAT_ID]
    }

    suspend fun saveCredentials(botToken: String, chatId: String) {
        context.dataStore.edit { preferences ->
            preferences[BOT_TOKEN] = botToken
            preferences[CHAT_ID] = chatId
        }
    }

    val lastSyncTimeFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIME] ?: 0L
    }

    suspend fun saveLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = timestamp
        }
    }

    fun getTopicIdFlow(folderName: String): Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[androidx.datastore.preferences.core.intPreferencesKey("topic_$folderName")]
    }

    suspend fun saveTopicId(folderName: String, topicId: Int) {
        context.dataStore.edit { preferences ->
            preferences[androidx.datastore.preferences.core.intPreferencesKey("topic_$folderName")] = topicId
        }
    }

    val syncStatusFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SYNC_STATUS] ?: "Idle"
    }

    suspend fun saveSyncStatus(status: String) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_STATUS] = status
        }
    }

    val syncProgressFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SYNC_PROGRESS] ?: ""
    }

    suspend fun saveSyncProgress(progress: String) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_PROGRESS] = progress
        }
    }

    val syncErrorFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SYNC_ERROR] ?: ""
    }

    suspend fun saveSyncError(error: String) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_ERROR] = error
        }
    }

    val syncingActiveFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SYNCING_ACTIVE] ?: false
    }

    suspend fun saveSyncingActive(isActive: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYNCING_ACTIVE] = isActive
        }
    }

    val syncIntervalFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SYNC_INTERVAL] ?: 6
    }

    suspend fun saveSyncInterval(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_INTERVAL] = hours
        }
    }

    val excludedFoldersFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[EXCLUDED_FOLDERS] ?: emptySet()
    }

    suspend fun saveExcludedFolders(folders: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[EXCLUDED_FOLDERS] = folders
        }
    }
}
