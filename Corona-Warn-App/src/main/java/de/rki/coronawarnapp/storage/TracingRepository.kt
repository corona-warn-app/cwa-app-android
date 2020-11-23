package de.rki.coronawarnapp.storage

import android.content.Context
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysTask
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.risk.TimeVariables.getActiveTracingDaysInRetentionPeriod
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskInfo
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
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
import java.util.Date
import java.util.NoSuchElementException
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
    private val timeStamper: TimeStamper
) {

    val lastTimeDiagnosisKeysFetched: Flow<Date?> = LocalData.lastTimeDiagnosisKeysFromServerFetchFlow()

    private val internalActiveTracingDaysInRetentionPeriod = MutableStateFlow(0L)
    val activeTracingDaysInRetentionPeriod: Flow<Long> = internalActiveTracingDaysInRetentionPeriod

    private val internalIsRefreshing =
        taskController.tasks.map { it.isDownloadDiagnosisKeysTaskRunning() || it.isRiskLevelTaskRunning() }

    val tracingProgress: Flow<TracingProgress> = combine(
        internalIsRefreshing,
        enfClient.isPerformingExposureDetection()
    ) { isDownloading, isCalculating ->
        when {
            isDownloading -> TracingProgress.Downloading
            isCalculating -> TracingProgress.ENFIsCalculating
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
     *
     * @see RiskLevelRepository
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
            TimerHelper.startManualKeyRetrievalTimer()
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
     *
     * @see RiskLevelRepository
     */
    // TODO temp place, this needs to go somewhere better
    fun refreshRiskLevel() {
        // check if the network is enabled to make the server fetch
        val isNetworkEnabled = ConnectivityHelper.isNetworkEnabled(context)

        // only fetch the diagnosis keys if background jobs are enabled, so that in manual
        // model the keys are only fetched on button press of the user
        val isBackgroundJobEnabled = ConnectivityHelper.autoModeEnabled(context)

        val wasNotYetFetched = LocalData.lastTimeDiagnosisKeysFromServerFetch() == null

        Timber.tag(TAG).v("Network is enabled $isNetworkEnabled")
        Timber.tag(TAG).v("Background jobs are enabled $isBackgroundJobEnabled")
        Timber.tag(TAG).v("Was not yet fetched from server $wasNotYetFetched")

        if (isNetworkEnabled && isBackgroundJobEnabled) {
            scope.launch {
                if (wasNotYetFetched || downloadDiagnosisKeysTaskDidNotRunRecently()) {
                    Timber.tag(TAG).v("Start the fetching and submitting of the diagnosis keys")

                    taskController.submitBlocking(
                        DefaultTaskRequest(
                            DownloadDiagnosisKeysTask::class,
                            DownloadDiagnosisKeysTask.Arguments(),
                            originTag = "TracingRepository.refreshRisklevel()"
                        )
                    )
                    TimerHelper.checkManualKeyRetrievalTimer()

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
                .mapNotNull { it.taskState.finishedAt }
                .sortedDescending()
                .first()
        } catch (e: NoSuchElementException) {
            Timber.tag(TAG).v("download did not run recently - no task with a finishedAt date found")
            return true
        }

        return currentDate.isAfter(taskLastFinishedAt.plus(Duration.standardHours(1))).also {
            Timber.tag(TAG)
                .v("download did not run recently: %s (last=%s, now=%s)", it, taskLastFinishedAt, currentDate)
        }
    }

    /**
     * Exposure summary
     * Refresh the following variables in TracingRepository
     * - daysSinceLastExposure
     * - matchedKeysCount
     *
     * @see TracingRepository
     */
    fun refreshExposureSummary() {
        scope.launch {
            try {
                val token = LocalData.googleApiToken()
                if (token != null) {
                    ExposureSummaryRepository.getExposureSummaryRepository()
                        .getLatestExposureSummary(token)
                }
                Timber.tag(TAG).v("retrieved latest exposure summary from db")
            } catch (e: Exception) {
                e.report(
                    ExceptionCategory.EXPOSURENOTIFICATION,
                    TAG,
                    null
                )
            }
        }
    }

    fun refreshLastSuccessfullyCalculatedScore() {
        RiskLevelRepository.refreshLastSuccessfullyCalculatedScore()
    }

    companion object {
        private val TAG: String? = TracingRepository::class.simpleName
    }
}
