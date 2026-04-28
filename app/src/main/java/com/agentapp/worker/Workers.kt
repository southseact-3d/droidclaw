package com.agentapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.agentapp.MainActivity
import com.agentapp.R
import com.agentapp.agent.AgentCore
import com.agentapp.data.db.ScheduledJobDao
import com.agentapp.data.models.JobStatus
import com.agentapp.data.models.JobType
import com.agentapp.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

// ── Heartbeat Worker ──────────────────────────────────────────────────────────

@HiltWorker
class HeartbeatWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val agentCore: AgentCore,
    private val settingsRepo: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Check active hours
        val startHour = settingsRepo.activeHoursStart.first()
        val endHour = settingsRepo.activeHoursEnd.first()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentHour < startHour || currentHour >= endHour) {
            return Result.success() // Outside active hours — skip silently
        }

        val heartbeatMd = settingsRepo.heartbeatMd.first()
        val result = agentCore.runHeartbeat(heartbeatMd)

        if (result.needsAttention && result.summary != null) {
            val notificationsEnabled = settingsRepo.notificationsEnabled.first()
            if (notificationsEnabled) {
                postNotification(
                    context = applicationContext,
                    title = "Agent needs your attention",
                    body = result.summary.take(200),
                    notifId = HEARTBEAT_NOTIF_ID
                )
            }
        }

        return if (result.error != null) Result.retry() else Result.success()
    }

    companion object {
        const val WORK_NAME = "heartbeat_worker"
        const val HEARTBEAT_NOTIF_ID = 1001

        fun schedule(context: Context, intervalMinutes: Int) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<HeartbeatWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES,
                (intervalMinutes / 2).toLong(), TimeUnit.MINUTES  // flex window
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

// ── Cron Worker ───────────────────────────────────────────────────────────────

@HiltWorker
class CronWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val agentCore: AgentCore,
    private val scheduledJobDao: ScheduledJobDao,
    private val settingsRepo: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val jobId = inputData.getString(KEY_JOB_ID) ?: return Result.failure()
        val job = scheduledJobDao.getJob(jobId) ?: return Result.failure()

        if (job.status != JobStatus.ACTIVE) return Result.success()

        val result = agentCore.runCronJob(job)
        scheduledJobDao.recordRun(jobId, System.currentTimeMillis())

        if (result.success && result.result != null && job.notifyOnResult) {
            val notificationsEnabled = settingsRepo.notificationsEnabled.first()
            if (notificationsEnabled) {
                postNotification(
                    context = applicationContext,
                    title = "📋 ${job.name}",
                    body = result.result.take(200),
                    notifId = jobId.hashCode()
                )
            }
        }

        return if (result.success) Result.success() else Result.retry()
    }

    companion object {
        const val KEY_JOB_ID = "job_id"

        fun scheduleOnce(context: Context, jobId: String, delayMs: Long) {
            val data = workDataOf(KEY_JOB_ID to jobId)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<CronWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "cron_$jobId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun cancel(context: Context, jobId: String) {
            WorkManager.getInstance(context).cancelUniqueWork("cron_$jobId")
        }
    }
}

// ── Notification helper ───────────────────────────────────────────────────────

fun postNotification(context: Context, title: String, body: String, notifId: Int) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Agent notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications from your AI agent"
        }
        manager.createNotificationChannel(channel)
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pi = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notif = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setContentIntent(pi)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    manager.notify(notifId, notif)
}

const val CHANNEL_ID = "agent_channel"

// ── Boot receiver — reschedules workers after reboot ─────────────────────────

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // WorkManager handles its own rescheduling after boot,
            // but we re-enqueue to be safe
            WorkManager.getInstance(context).cancelAllWorkByTag("heartbeat")
        }
    }
}
