package de.rki.coronawarnapp.storage

import android.content.Context
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysTask
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.lastSubmission
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.risk.TimeVariables.getActiveTracingDaysInRetentionPeriod
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskInfo
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Tracing Repository refreshes and triggers all tracing relevant data. Some functions get their
 * data directly from the Exposure Notification, others consume the shared preferences.
 *
 * @see LocalData
 * @see InternalExposureNotificationClient
 * @see RiskLevelRepository
 */
@Singleton
class TracingRepository @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val scope: CoroutineScope,
    private val taskController: TaskController,
    enfClient: ENFClient,
    private val timeStamper: TimeStamper,
    private val exposureDetectionTracker: ExposureDetectionTracker,
    private val backgroundModeStatus: BackgroundModeStatus
) {

    private val internalActiveTracingDaysInRetentionPeriod = MutableStateFlow(0L)
    val activeTracingDaysInRetentionPeriod: Flow<Long> = internalActiveTracingDaysInRetentionPeriod

    val tracingProgress: Flow<TracingProgress> = combine(
        taskController.tasks.map { it.isDownloadDiagnosisKeysTaskRunning() },
        enfClient.isPerformingExposureDetection(),
        taskController.tasks.map { it.isRiskLevelTaskRunning() }
    ) { isDownloading, isExposureDetecting, isRiskLeveling ->
        when {
            isDownloading -> TracingProgress.Downloading
            isExposureDetecting || isRiskLeveling -> TracingProgress.ENFIsCalculating
            else -> TracingProgress.Idle
        }
    }

    private fun List<TaskInfo>.isRiskLevelTaskRunning() = any {
        it.taskState.isActive && it.taskState.request.type == RiskLevelTask::class
    }

    private fun List<TaskInfo>.isDownloadDiagnosisKeysTaskRunning() = any {
        it.taskState.isActive && it.taskState.request.type == DownloadDiagnosisKeysTask::class
    }

    /**
     * Refresh the diagnosis keys. For that isRefreshing is set to true which is displayed in the ui.
     * Afterwards the RetrieveDiagnosisKeysTransaction and the RiskLevelTransaction are started.
     * Regardless of whether the transactions where successful or not the
     * lastTimeDiagnosisKeysFetchedDate is updated. But the the value will only be updated after a
     * successful go through from the RetrievelDiagnosisKeysTransaction.
     */
    fun refreshDiagnosisKeys() {
        scope.launch {
            taskController.submitBlocking(
                DefaultTaskRequest(
                    DownloadDiagnosisKeysTask::class,
                    DownloadDiagnosisKeysTask.Arguments(),
                    originTag = "TracingRepository.refreshDiagnosisKeys()"
                )
            )
            taskController.submit(
                DefaultTaskRequest(
                    RiskLevelTask::class, originTag = "TracingRepository.refreshDiagnosisKeys()"
                )
            )
        }
    }

    /**
     * Refresh the activeTracingDaysInRetentionPeriod calculation.
     *
     * @see de.rki.coronawarnapp.risk.TimeVariables
     */
    fun refreshActiveTracingDaysInRetentionPeriod() {
        scope.launch {
            internalActiveTracingDaysInRetentionPeriod.value =
                getActiveTracingDaysInRetentionPeriod()
        }
    }

    /**
     * Launches the RetrieveDiagnosisKeysTransaction and RiskLevelTransaction in the viewModel scope
     */
    // TODO temp place, this needs to go somewhere better
    suspend fun refreshRiskLevel() {
        // check if the network is enabled to make the server fetch
        val isNetworkEnabled = ConnectivityHelper.isNetworkEnabled(context)

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
                        DefaultTaskRequest(RiskLevelTask::class, originTag = "TracingRepository.refreshRiskLevel()")
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

        return currentDate.isAfter(taskLastFinishedAt.plus(Duration.standardHours(1))).also {
            Timber.tag(TAG)
                .v("download did not run recently: %s (last=%s, now=%s)", it, taskLastFinishedAt, currentDate)
        }
    }

    companion object {
        private val TAG: String? = TracingRepository::class.simpleName
    }
}
