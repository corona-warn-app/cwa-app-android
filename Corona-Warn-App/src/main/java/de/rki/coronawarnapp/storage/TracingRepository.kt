package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.risk.TimeVariables.getActiveTracingDaysInRetentionPeriod
import de.rki.coronawarnapp.task.TaskInfo
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppInjector
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
 * @see RetrieveDiagnosisKeysTransaction
 * @see RiskLevelRepository
 */
@Singleton
class TracingRepository @Inject constructor(
    @AppScope private val scope: CoroutineScope,
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
        retrievingDiagnosisKeys.combine(AppInjector.component.taskController.tasks) { retrievingDiagnosisKeys, tasks ->
            retrievingDiagnosisKeys || tasks.isRiskLevelTaskRunning()
        }
    val isRefreshing: Flow<Boolean> = combine(
        internalIsRefreshing,
        enfClient.isCurrentlyCalculating()
    ) { isRefreshing, isCalculating ->
        isRefreshing || isCalculating
    }

    private fun List<TaskInfo>.isRiskLevelTaskRunning() = find {
        it.taskState.isActive && it.taskState.request.type == RiskLevelTask::class
    } != null

    /**
     * Refresh the diagnosis keys. For that isRefreshing is set to true which is displayed in the ui.
     * Afterwards the RetrieveDiagnosisKeysTransaction and the RiskLevelTransaction are started.
     * Regardless of whether the transactions where successful or not the
     * lastTimeDiagnosisKeysFetchedDate is updated. But the the value will only be updated after a
     * successful go through from the RetrievelDiagnosisKeysTransaction.
     *
     * @see RetrieveDiagnosisKeysTransaction
     * @see RiskLevelRepository
     */
    fun refreshDiagnosisKeys() {
        scope.launch {
            retrievingDiagnosisKeys.value = true
            try {
                RetrieveDiagnosisKeysTransaction.start()
                AppInjector.component.taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
            } catch (e: Exception) {
                e.report(ExceptionCategory.EXPOSURENOTIFICATION)
            }
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
     * @see RiskLevelTransaction
     * @see RiskLevelRepository
     */
    // TODO temp place, this needs to go somewhere better
    fun refreshRiskLevel() {
        scope.launch {
            try {

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
                val isNetworkEnabled =
                    ConnectivityHelper.isNetworkEnabled(CoronaWarnApplication.getAppContext())

                // only fetch the diagnosis keys if background jobs are enabled, so that in manual
                // model the keys are only fetched on button press of the user
                val isBackgroundJobEnabled =
                    ConnectivityHelper.autoModeEnabled(CoronaWarnApplication.getAppContext())

                Timber.tag(TAG)
                    .v("Keys were not retrieved today $keysWereNotRetrievedToday")
                Timber.tag(TAG).v("Network is enabled $isNetworkEnabled")
                Timber.tag(TAG)
                    .v("Background jobs are enabled $isBackgroundJobEnabled")

                if (keysWereNotRetrievedToday && isNetworkEnabled && isBackgroundJobEnabled) {
                    // TODO shouldn't access this directly
                    retrievingDiagnosisKeys.value = true

                    // start the fetching and submitting of the diagnosis keys
                    RetrieveDiagnosisKeysTransaction.start()
                    refreshLastTimeDiagnosisKeysFetchedDate()
                    TimerHelper.checkManualKeyRetrievalTimer()
                }
            } catch (e: TransactionException) {
                e.cause?.report(ExceptionCategory.INTERNAL)
            } catch (e: Exception) {
                e.report(ExceptionCategory.INTERNAL)
            }

            AppInjector.component.taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
            // TODO shouldn't access this directly
            retrievingDiagnosisKeys.value = false
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
