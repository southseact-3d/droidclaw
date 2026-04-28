package com.agentapp.ui.scheduler

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentapp.data.db.ScheduledJobDao
import com.agentapp.data.models.*
import com.agentapp.data.repository.SettingsRepository
import com.agentapp.worker.CronWorker
import com.agentapp.worker.HeartbeatWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SchedulerUiState(
    val jobs: List<ScheduledJob> = emptyList(),
    val heartbeatEnabled: Boolean = false,
    val heartbeatIntervalMinutes: Int = 60,
    val heartbeatMd: String = "",
    val activeHoursStart: Int = 8,
    val activeHoursEnd: Int = 22,
    val showAddDialog: Boolean = false,
    val editingJob: ScheduledJob? = null
)

@HiltViewModel
class SchedulerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scheduledJobDao: ScheduledJobDao,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SchedulerUiState())
    val state: StateFlow<SchedulerUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                scheduledJobDao.getAllJobs(),
                settingsRepo.heartbeatEnabled,
                settingsRepo.heartbeatIntervalMinutes,
                settingsRepo.heartbeatMd,
                settingsRepo.activeHoursStart
            ) { jobs, hbEnabled, hbInterval, hbMd, activeStart ->
                _state.update {
                    it.copy(
                        jobs = jobs,
                        heartbeatEnabled = hbEnabled,
                        heartbeatIntervalMinutes = hbInterval,
                        heartbeatMd = hbMd,
                        activeHoursStart = activeStart
                    )
                }
            }.collect()
        }
        viewModelScope.launch {
            settingsRepo.activeHoursEnd.collect { end ->
                _state.update { it.copy(activeHoursEnd = end) }
            }
        }
    }

    fun setHeartbeatEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setHeartbeatEnabled(enabled)
            if (enabled) {
                HeartbeatWorker.schedule(context, _state.value.heartbeatIntervalMinutes)
            } else {
                HeartbeatWorker.cancel(context)
            }
        }
    }

    fun setHeartbeatInterval(minutes: Int) {
        viewModelScope.launch {
            settingsRepo.setHeartbeatInterval(minutes)
            if (_state.value.heartbeatEnabled) {
                HeartbeatWorker.schedule(context, minutes)
            }
        }
    }

    fun setHeartbeatMd(content: String) {
        viewModelScope.launch { settingsRepo.setHeartbeatMd(content) }
    }

    fun setActiveHours(start: Int, end: Int) {
        viewModelScope.launch { settingsRepo.setActiveHours(start, end) }
    }

    fun addCronJob(name: String, prompt: String, intervalMinutes: Int, notifyOnResult: Boolean) {
        if (name.isBlank() || prompt.isBlank()) return
        viewModelScope.launch {
            val job = ScheduledJob(
                id = UUID.randomUUID().toString(),
                name = name,
                type = JobType.CRON,
                intervalMinutes = intervalMinutes,
                prompt = prompt,
                notifyOnResult = notifyOnResult
            )
            scheduledJobDao.insert(job)
            scheduleJob(job)
            _state.update { it.copy(showAddDialog = false) }
        }
    }

    fun toggleJob(job: ScheduledJob) {
        viewModelScope.launch {
            val newStatus = if (job.status == JobStatus.ACTIVE) JobStatus.PAUSED else JobStatus.ACTIVE
            scheduledJobDao.update(job.copy(status = newStatus))
            if (newStatus == JobStatus.ACTIVE) scheduleJob(job)
            else CronWorker.cancel(context, job.id)
        }
    }

    fun deleteJob(jobId: String) {
        viewModelScope.launch {
            CronWorker.cancel(context, jobId)
            scheduledJobDao.delete(jobId)
        }
    }

    fun runNow(job: ScheduledJob) {
        CronWorker.scheduleOnce(context, job.id, delayMs = 0)
    }

    fun showAddDialog() = _state.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _state.update { it.copy(showAddDialog = false) }

    private fun scheduleJob(job: ScheduledJob) {
        val delayMs = job.intervalMinutes * 60 * 1000L
        CronWorker.scheduleOnce(context, job.id, delayMs)
    }
}
