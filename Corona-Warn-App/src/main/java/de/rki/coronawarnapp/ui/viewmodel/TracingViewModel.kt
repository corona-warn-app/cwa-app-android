package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.ExceptionCategory.INTERNAL
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import de.rki.coronawarnapp.util.ConnectivityHelper
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import timber.log.Timber
import java.util.Date

/**
 * Provides all the relevant data for tracing relevant topics and settings.
 * The public variables consume MutableLiveDate from two different repositories.
 * This variables are consumed in different views all over the application via bindings, e.g. "@{tracingViewModel.isTracingEnabled}"
 * Most of the values are used in formatters due to the different states the application can have.
 *
 * @see TracingRepository
 * @see RiskLevelRepository
 */
class TracingViewModel : ViewModel() {

    companion object {
        val TAG: String? = TracingViewModel::class.simpleName
    }

    // Values from RiskLevelRepository
    val riskLevel: LiveData<Int> = RiskLevelRepository.riskLevelScore
    val riskLevelScoreLastSuccessfulCalculated =
        RiskLevelRepository.riskLevelScoreLastSuccessfulCalculated

    // Values from ExposureSummaryRepository
    val daysSinceLastExposure: LiveData<Int?> = ExposureSummaryRepository.daysSinceLastExposure
    val matchedKeyCount: LiveData<Int?> = ExposureSummaryRepository.matchedKeyCount

    // Values from TracingRepository
    val lastTimeDiagnosisKeysFetched: LiveData<Date> =
        TracingRepository.lastTimeDiagnosisKeysFetched
    val isTracingEnabled: LiveData<Boolean?> = TracingRepository.isTracingEnabled
    val activeTracingDaysInRetentionPeriod = TracingRepository.activeTracingDaysInRetentionPeriod
    var isRefreshing: LiveData<Boolean> = TracingRepository.isRefreshing

    /**
     * Launches the RetrieveDiagnosisKeysTransaction and RiskLevelTransaction in the viewModel scope
     *
     * @see RiskLevelTransaction
     * @see RiskLevelRepository
     */
    fun refreshRiskLevel() {
        viewModelScope.launch {
            try {
                val currentDate = DateTime(Instant.now(), DateTimeZone.getDefault())
                val lastFetch = DateTime(
                    LocalData.lastTimeDiagnosisKeysFromServerFetch(),
                    DateTimeZone.getDefault()
                )
                val keysWereNotRetrievedToday =
                    LocalData.lastTimeDiagnosisKeysFromServerFetch() == null ||
                            currentDate.withTimeAtStartOfDay() != lastFetch.withTimeAtStartOfDay()
                val isNetworkEnabled =
                    ConnectivityHelper.isNetworkEnabled(CoronaWarnApplication.getAppContext())
                val isBackgroundJobEnabled =
                    ConnectivityHelper.isBackgroundJobEnabled(CoronaWarnApplication.getAppContext())
                if (keysWereNotRetrievedToday && isNetworkEnabled && isBackgroundJobEnabled) {
                    TracingRepository.isRefreshing.value = true
                    RetrieveDiagnosisKeysTransaction.start()
                    refreshLastTimeDiagnosisKeysFetchedDate()
                    TimerHelper.checkManualKeyRetrievalTimer()
                }
            } catch (e: TransactionException) {
                e.cause?.report(INTERNAL)
            } catch (e: Exception) {
                e.report(INTERNAL)
            }

            try {
                RiskLevelTransaction.start()
            } catch (e: TransactionException) {
                e.cause?.report(INTERNAL)
            } catch (e: Exception) {
                e.report(INTERNAL)
            }

            TracingRepository.isRefreshing.value = false
        }
    }

    /**
     * Refreshes the time when the diagnosis key was fetched the last time
     *
     * @see TracingRepository
     */
    fun refreshLastTimeDiagnosisKeysFetchedDate() {
        TracingRepository.refreshLastTimeDiagnosisKeysFetchedDate()
    }

    /**
     * Refreshes the diagnosis keys
     *
     * @see TracingRepository
     */
    fun refreshDiagnosisKeys() {
        this.viewModelScope.launch {
            TracingRepository.refreshDiagnosisKeys()
            TimerHelper.startManualKeyRetrievalTimer()
        }
    }

    /**
     * Refreshes is tracing enabled
     *
     * @see TracingRepository
     */
    fun refreshIsTracingEnabled() {
        viewModelScope.launch {
            TracingRepository.refreshIsTracingEnabled()
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
        viewModelScope.launch {
            try {
                ExposureSummaryRepository.getExposureSummaryRepository()
                    .getLatestExposureSummary()
                Timber.v("retrieved latest exposure summary from db")
            } catch (e: Exception) {
                e.report(
                    de.rki.coronawarnapp.exception.ExceptionCategory.EXPOSURENOTIFICATION,
                    TAG,
                    null
                )
            }
        }
    }

    /**
     * Refresh the activeTracingDaysInRetentionPeriod in the viewModel scope
     *
     * @see TracingRepository
     */
    fun refreshActiveTracingDaysInRetentionPeriod() {
        viewModelScope.launch {
            TracingRepository.refreshActiveTracingDaysInRetentionPeriod()
        }
    }

    fun refreshLastSuccessfullyCalculatedScore() {
        RiskLevelRepository.refreshLastSuccessfullyCalculatedScore()
    }
}
