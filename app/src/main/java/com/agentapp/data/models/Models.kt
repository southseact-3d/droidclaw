package com.agentapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ── Provider & model config ──────────────────────────────────────────────────

enum class ProviderType { NVIDIA_NIM, OPENROUTER, KILO_GATEWAY }

data class ProviderConfig(
    val type: ProviderType,
    val apiKey: String,
    val model: String,
    val baseUrl: String,
    val enabled: Boolean = true,
    val priority: Int = 0          // lower = tried first
)

// ── Chat messages ─────────────────────────────────────────────────────────────

enum class Role { USER, ASSISTANT, SYSTEM, TOOL_CALL, TOOL_RESULT }
enum class MessageSource { CHAT, HEARTBEAT, CRON }

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: Role,
    val content: String,
    val source: MessageSource = MessageSource.CHAT,
    val timestamp: Long = System.currentTimeMillis(),
    val providerUsed: String? = null,
    val tokenCount: Int? = null,
    val isStreaming: Boolean = false
)

// ── Skills ────────────────────────────────────────────────────────────────────

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val markdownContent: String,
    val toolDefinitions: String = "[]",   // JSON array of ToolDefinition
    val enabled: Boolean = true,
    val installedAt: Long = System.currentTimeMillis(),
    val sourceUrl: String? = null,
    val version: String = "1.0.0"
)

data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, Any> = emptyMap()
)

// ── Scheduler ─────────────────────────────────────────────────────────────────

enum class JobType { HEARTBEAT, CRON }
enum class JobStatus { ACTIVE, PAUSED, COMPLETED, FAILED }

@Entity(tableName = "scheduled_jobs")
data class ScheduledJob(
    @PrimaryKey val id: String,
    val name: String,
    val type: JobType,
    val cronExpression: String? = null,   // null = heartbeat
    val intervalMinutes: Int = 60,
    val prompt: String,
    val status: JobStatus = JobStatus.ACTIVE,
    val lastRunAt: Long? = null,
    val nextRunAt: Long? = null,
    val runCount: Int = 0,
    val notifyOnResult: Boolean = true,
    val activeHoursStart: Int = 8,        // 24h
    val activeHoursEnd: Int = 22
)

// ── Agent sessions ────────────────────────────────────────────────────────────

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey val id: String,
    val name: String = "Main",
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val messageCount: Int = 0
)

// ── Type converters ───────────────────────────────────────────────────────────

class Converters {
    private val gson = Gson()

    @TypeConverter fun fromRole(v: Role) = v.name
    @TypeConverter fun toRole(v: String) = Role.valueOf(v)
    @TypeConverter fun fromSource(v: MessageSource) = v.name
    @TypeConverter fun toSource(v: String) = MessageSource.valueOf(v)
    @TypeConverter fun fromJobType(v: JobType) = v.name
    @TypeConverter fun toJobType(v: String) = JobType.valueOf(v)
    @TypeConverter fun fromJobStatus(v: JobStatus) = v.name
    @TypeConverter fun toJobStatus(v: String) = JobStatus.valueOf(v)
    @TypeConverter fun fromMap(v: Map<String, String>?): String = gson.toJson(v)
    @TypeConverter fun toMap(v: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(v, type) ?: emptyMap()
    }
}
