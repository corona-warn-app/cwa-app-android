package de.rki.coronawarnapp.storage

import android.annotation.SuppressLint
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysTask
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.lastSubmission
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingRiskWorkScheduler
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningTask
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningTaskProgress
import de.rki.coronawarnapp.risk.EwRiskLevelTask
import de.rki.coronawarnapp.risk.execution.ExposureWindowRiskWorkScheduler
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskInfo
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Tracing Repository refreshes and triggers all tracing relevant data. Some functions get their
 * data directly from the Exposure Notification, others consume the shared preferences.
 */
@Singleton
class TracingRepository @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val taskController: TaskController,
    enfClient: ENFClient,
    private val timeStamper: TimeStamper,
    private val exposureDetectionTracker: ExposureDetectionTracker,
    private val backgroundModeStatus: BackgroundModeStatus,
    private val exposureWindowRiskWorkScheduler: ExposureWindowRiskWorkScheduler,
    private val presenceTracingRiskWorkScheduler: PresenceTracingRiskWorkScheduler,
    private val networkStateProvider: NetworkStateProvider,
) {

    @SuppressLint("BinaryOperationInTimber")
    val tracingProgress: Flow<TracingProgress> = combine(
        taskController.tasks,
        enfClient.isPerformingExposureDetection(),
    ) { taskInfos, isExposureDetecting ->

        val isEWDownloading = taskInfos.isEWDownloadingPackages()
        val isEWCalculatingRisk = taskInfos.isEWCalculatingRisk()

        val isPTDownloading = taskInfos.isPTDownloadingPackages()
        val isPTCalculatingRisk = taskInfos.isPTCalculatingRisk()

        when {
            isEWDownloading || isPTDownloading -> TracingProgress.Downloading
            isExposureDetecting || isEWCalculatingRisk || isPTCalculatingRisk -> TracingProgress.IsCalculating
            else -> TracingProgress.Idle
        }.also {
            Timber.tag(TAG).v(
                "TracingProgress: $it, isExposureDetecting=$isExposureDetecting, " +
                    "isEWDownloading=$isEWDownloading, isEWCalculatingRisk=$isEWCalculatingRisk, " +
                    "isPTDownloading=$isPTDownloading, isPTCalculatingRisk=$isPTCalculatingRisk"
            )
        }
    }

    private suspend fun List<TaskInfo>.isPTDownloadingPackages() = any {
        it.taskState.isActive && it.taskState.request.type == PresenceTracingWarningTask::class &&
            it.progress.firstOrNull() is PresenceTracingWarningTaskProgress.Downloading
    }

    private suspend fun List<TaskInfo>.isPTCalculatingRisk() = any {
        it.taskState.isActive && it.taskState.request.type == PresenceTracingWarningTask::class &&
            it.progress.firstOrNull() is PresenceTracingWarningTaskProgress.Calculating
    }

    private fun List<TaskInfo>.isEWDownloadingPackages() = any {
        it.taskState.isActive && it.taskState.request.type == DownloadDiagnosisKeysTask::class
    }

    private fun List<TaskInfo>.isEWCalculatingRisk() = any {
        it.taskState.isActive && it.taskState.request.type == EwRiskLevelTask::class
    }

    fun refreshRiskResult() = scope.launch {
        Timber.tag(TAG).d("refreshRiskResults()")

        exposureWindowRiskWorkScheduler.runRiskTasksNow(sourceTag = TAG)
        presenceTracingRiskWorkScheduler.runRiskTaskNow(sourceTag = TAG)
    }

    /**
     * Launches the RetrieveDiagnosisKeysTransaction and RiskLevelTransaction in the viewModel scope
     */
    // TODO temp place, this needs to go somewhere better
    suspend fun refreshRiskLevel() {
        // check if the network is enabled to make the server fetch
        val isNetworkEnabled = networkStateProvider.networkState.first().isInternetAvailable

        // only fetch the diagnosis keys if background jobs are enabled, so that in manual
        // model the keys are only fetched on button press of the user
        val isBackgroundJobEnabled = backgroundModeStatus.isAutoModeEnabled.first()

        Timber.tag(TAG).v("Network is enabled $isNetworkEnabled")
        Timber.tag(TAG).v("Background jobs are enabled $isBackgroundJobEnabled")

        if (isNetworkEnabled && isBackgroundJobEnabled) {
            scope.launch {
                val lastSubmission = exposureDetectionTracker.lastSubmission(onlyFinished = false)
                Timber.tag(TAG).v("Last submission was %s", lastSubmission)

                if (lastSubmission == null || downloadDiagnosisKeysTaskDidNotRunRecently()) {
                    Timber.tag(TAG).v("Start the fetching and submitting of the diagnosis keys")

                    taskController.submitBlocking(
                        DefaultTaskRequest(
                            DownloadDiagnosisKeysTask::class,
                            DownloadDiagnosisKeysTask.Arguments(),
                            originTag = "TracingRepository.refreshRisklevel()"
                        )
                    )

                    taskController.submit(
                        DefaultTaskRequest(EwRiskLevelTask::class, originTag = "TracingRepository.refreshRiskLevel()")
                    )
                }
            }
        }
    }

    private suspend fun downloadDiagnosisKeysTaskDidNotRunRecently(): Boolean {
        val currentDate = timeStamper.nowUTC
        val taskLastFinishedAt = try {
            taskController.tasks.first()
                .filter { it.taskState.type == DownloadDiagnosisKeysTask::class }
                .mapNotNull { it.taskState.finishedAt ?: it.taskState.startedAt }
                .maxOrNull()!!
        } catch (e: NullPointerException) {
            Timber.tag(TAG).v("download did not run recently - no task with a date found")
            return true
        }

        return currentDate.isAfter(taskLastFinishedAt.plus(Duration.ofHours(1))).also {
            Timber.tag(TAG)
                .v("download did not run recently: %s (last=%s, now=%s)", it, taskLastFinishedAt, currentDate)
        }
    }

    companion object {
        private const val TAG: String = "TracingRepository"
    }
}
