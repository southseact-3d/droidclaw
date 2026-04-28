package com.agentapp.data.db

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import com.agentapp.data.models.*
import kotlinx.coroutines.flow.Flow

// ── DAOs ──────────────────────────────────────────────────────────────────────

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessages(sessionId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesOnce(sessionId: String): List<Message>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(sessionId: String, limit: Int = 20): List<Message>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message): Long

    @Update
    suspend fun update(message: Message)

    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun clearSession(sessionId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE sessionId = :sessionId")
    suspend fun countMessages(sessionId: String): Int
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY name ASC")
    fun getAllSkills(): Flow<List<Skill>>

    @Query("SELECT * FROM skills WHERE enabled = 1 ORDER BY name ASC")
    suspend fun getEnabledSkills(): List<Skill>

    @Query("SELECT * FROM skills WHERE id = :id")
    suspend fun getSkill(id: String): Skill?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(skill: Skill)

    @Update
    suspend fun update(skill: Skill)

    @Query("DELETE FROM skills WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE skills SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)
}

@Dao
interface ScheduledJobDao {
    @Query("SELECT * FROM scheduled_jobs ORDER BY name ASC")
    fun getAllJobs(): Flow<List<ScheduledJob>>

    @Query("SELECT * FROM scheduled_jobs WHERE status = 'ACTIVE'")
    suspend fun getActiveJobs(): List<ScheduledJob>

    @Query("SELECT * FROM scheduled_jobs WHERE id = :id")
    suspend fun getJob(id: String): ScheduledJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: ScheduledJob)

    @Update
    suspend fun update(job: ScheduledJob)

    @Query("DELETE FROM scheduled_jobs WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE scheduled_jobs SET lastRunAt = :lastRun, runCount = runCount + 1 WHERE id = :id")
    suspend fun recordRun(id: String, lastRun: Long)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY lastActiveAt DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSession(id: String): Session?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: Session)

    @Update
    suspend fun update(session: Session)

    @Query("UPDATE sessions SET lastActiveAt = :time WHERE id = :id")
    suspend fun touch(id: String, time: Long = System.currentTimeMillis())
}

// ── Database ──────────────────────────────────────────────────────────────────

@Database(
    entities = [Message::class, Skill::class, ScheduledJob::class, Session::class, MpcServer::class, MpcTool::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = AgentDatabase.AutoMigration1To2::class)
    ]
)
@TypeConverters(Converters::class)
abstract class AgentDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun skillDao(): SkillDao
    abstract fun scheduledJobDao(): ScheduledJobDao
    abstract fun sessionDao(): SessionDao
    abstract fun mpcDao(): MpcDao

    // Auto-migration from version 1 to 2
    @RenameTable(from = "skill", to = "skills")
    @RenameTable(from = "scheduled_job", to = "scheduled_jobs")
    @RenameTable(from = "session", to = "sessions")
    @RenameTable(from = "message", to = "messages")
    class AutoMigration1To2 : AutoMigrationSpec
}
