package com.agentapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.agentapp.data.models.ProviderConfig
import com.agentapp.data.models.ProviderType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "agent_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    companion object {
        val KEY_PROVIDERS = stringPreferencesKey("providers")
        val KEY_HEARTBEAT_ENABLED = booleanPreferencesKey("heartbeat_enabled")
        val KEY_HEARTBEAT_INTERVAL = intPreferencesKey("heartbeat_interval_minutes")
        val KEY_HEARTBEAT_MD = stringPreferencesKey("heartbeat_md")
        val KEY_ACTIVE_SESSION = stringPreferencesKey("active_session")
        val KEY_ACTIVE_HOURS_START = intPreferencesKey("active_hours_start")
        val KEY_ACTIVE_HOURS_END = intPreferencesKey("active_hours_end")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_SYSTEM_PROMPT = stringPreferencesKey("system_prompt")

        val DEFAULT_PROVIDERS = listOf(
            ProviderConfig(
                type = ProviderType.NVIDIA_NIM,
                apiKey = "",
                model = "meta/llama-3.3-70b-instruct",
                baseUrl = "https://integrate.api.nvidia.com/v1",
                enabled = false,
                priority = 0
            ),
            ProviderConfig(
                type = ProviderType.OPENROUTER,
                apiKey = "",
                model = "anthropic/claude-3.5-sonnet",
                baseUrl = "https://openrouter.ai/api/v1",
                enabled = false,
                priority = 1
            ),
            ProviderConfig(
                type = ProviderType.KILO_GATEWAY,
                apiKey = "",
                model = "claude-3-5-sonnet-20241022",
                baseUrl = "https://api.kilo.dev/v1",
                enabled = false,
                priority = 2
            )
        )

        val DEFAULT_SYSTEM_PROMPT = """
You are a personal AI assistant running on Android. You are helpful, concise, and action-oriented.

When running a heartbeat check, review the HEARTBEAT.md checklist if provided and respond with HEARTBEAT_OK if there is nothing requiring attention. Only notify the user if something genuinely needs their attention.

When using tools from skills, call them precisely and report results clearly.
        """.trimIndent()
    }

    val providers: Flow<List<ProviderConfig>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_PROVIDERS]
        if (json.isNullOrBlank()) {
            DEFAULT_PROVIDERS
        } else {
            try {
                val type = object : TypeToken<List<ProviderConfig>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                DEFAULT_PROVIDERS
            }
        }
    }

    val heartbeatEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_HEARTBEAT_ENABLED] ?: false
    }

    val heartbeatIntervalMinutes: Flow<Int> = context.dataStore.data.map {
        it[KEY_HEARTBEAT_INTERVAL] ?: 60
    }

    val heartbeatMd: Flow<String> = context.dataStore.data.map {
        it[KEY_HEARTBEAT_MD] ?: ""
    }

    val activeHoursStart: Flow<Int> = context.dataStore.data.map {
        it[KEY_ACTIVE_HOURS_START] ?: 8
    }

    val activeHoursEnd: Flow<Int> = context.dataStore.data.map {
        it[KEY_ACTIVE_HOURS_END] ?: 22
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val systemPrompt: Flow<String> = context.dataStore.data.map {
        it[KEY_SYSTEM_PROMPT] ?: DEFAULT_SYSTEM_PROMPT
    }

    val activeSession: Flow<String> = context.dataStore.data.map {
        it[KEY_ACTIVE_SESSION] ?: "main"
    }

    suspend fun saveProviders(providers: List<ProviderConfig>) {
        context.dataStore.edit { it[KEY_PROVIDERS] = gson.toJson(providers) }
    }

    suspend fun setHeartbeatEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HEARTBEAT_ENABLED] = enabled }
    }

    suspend fun setHeartbeatInterval(minutes: Int) {
        context.dataStore.edit { it[KEY_HEARTBEAT_INTERVAL] = minutes }
    }

    suspend fun setHeartbeatMd(content: String) {
        context.dataStore.edit { it[KEY_HEARTBEAT_MD] = content }
    }

    suspend fun setActiveHours(start: Int, end: Int) {
        context.dataStore.edit {
            it[KEY_ACTIVE_HOURS_START] = start
            it[KEY_ACTIVE_HOURS_END] = end
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setSystemPrompt(prompt: String) {
        context.dataStore.edit { it[KEY_SYSTEM_PROMPT] = prompt }
    }

    suspend fun setActiveSession(sessionId: String) {
        context.dataStore.edit { it[KEY_ACTIVE_SESSION] = sessionId }
    }

    suspend fun getProvidersOnce(): List<ProviderConfig> {
        var result = DEFAULT_PROVIDERS
        context.dataStore.data.map { prefs ->
            val json = prefs[KEY_PROVIDERS]
            if (!json.isNullOrBlank()) {
                try {
                    val type = object : TypeToken<List<ProviderConfig>>() {}.type
                    result = gson.fromJson(json, type)
                } catch (_: Exception) {}
            }
        }.collect {}
        return result
    }
}
