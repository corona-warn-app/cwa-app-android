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
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import timber.log.Timber
import java.util.Date
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
    enfClient: ENFClient
) {

    private val internalLastTimeDiagnosisKeysFetched = MutableStateFlow<Date?>(null)
    val lastTimeDiagnosisKeysFetched: Flow<Date?> = internalLastTimeDiagnosisKeysFetched

    private val internalActiveTracingDaysInRetentionPeriod = MutableStateFlow(0L)
    val activeTracingDaysInRetentionPeriod: Flow<Long> = internalActiveTracingDaysInRetentionPeriod

    /**
     * Refresh the last time diagnosis keys fetched date with the current shared preferences state.
     *
     * @see LocalData
     */
    fun refreshLastTimeDiagnosisKeysFetchedDate() {
        internalLastTimeDiagnosisKeysFetched.value =
            LocalData.lastTimeDiagnosisKeysFromServerFetch()
    }

    private val retrievingDiagnosisKeys = MutableStateFlow(false)
    private val internalIsRefreshing =
        retrievingDiagnosisKeys.combine(taskController.tasks) { retrievingDiagnosisKeys, tasks ->
            retrievingDiagnosisKeys || tasks.isRiskLevelTaskRunning()
        }
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
            retrievingDiagnosisKeys.value = true
            taskController.submitBlocking(
                DefaultTaskRequest(
                    DownloadDiagnosisKeysTask::class,
                    DownloadDiagnosisKeysTask.Arguments()
                )
            )
            taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
            refreshLastTimeDiagnosisKeysFetchedDate()
            retrievingDiagnosisKeys.value = false
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

        // get the current date and the date the diagnosis keys were fetched the last time
        val currentDate = DateTime(Instant.now(), DateTimeZone.UTC)
        val lastFetch = DateTime(
            LocalData.lastTimeDiagnosisKeysFromServerFetch(),
            DateTimeZone.UTC
        )

        // check if the keys were not already retrieved today
        val keysWereNotRetrievedToday =
            LocalData.lastTimeDiagnosisKeysFromServerFetch() == null ||
                currentDate.withTimeAtStartOfDay() != lastFetch.withTimeAtStartOfDay()

        // check if the network is enabled to make the server fetch
        val isNetworkEnabled = ConnectivityHelper.isNetworkEnabled(context)

        // only fetch the diagnosis keys if background jobs are enabled, so that in manual
        // model the keys are only fetched on button press of the user
        val isBackgroundJobEnabled = ConnectivityHelper.autoModeEnabled(context)

        Timber.tag(TAG).v("Keys were not retrieved today $keysWereNotRetrievedToday")
        Timber.tag(TAG).v("Network is enabled $isNetworkEnabled")
        Timber.tag(TAG).v("Background jobs are enabled $isBackgroundJobEnabled")

        if (keysWereNotRetrievedToday && isNetworkEnabled && isBackgroundJobEnabled) {
            // TODO shouldn't access this directly
            retrievingDiagnosisKeys.value = true

            // start the fetching and submitting of the diagnosis keys
            scope.launch {
                taskController.submitBlocking(
                    DefaultTaskRequest(
                        DownloadDiagnosisKeysTask::class,
                        DownloadDiagnosisKeysTask.Arguments()
                    )
                )
                refreshLastTimeDiagnosisKeysFetchedDate()
                TimerHelper.checkManualKeyRetrievalTimer()

                taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
                // TODO shouldn't access this directly
                retrievingDiagnosisKeys.value = false
            }
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
