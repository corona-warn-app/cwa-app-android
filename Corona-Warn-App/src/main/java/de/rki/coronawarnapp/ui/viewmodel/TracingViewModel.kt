package de.rki.coronawarnapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rki.coronawarnapp.exception.ExceptionCategory.INTERNAL
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.report
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import kotlinx.coroutines.launch
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

    // TODO: comments for variables
    // Values from RiskLevelRepository
    val riskLevel: LiveData<Int> = RiskLevelRepository.riskLevelScore

    // Values from ExposureSummaryRepository
    val daysSinceLastExposure: LiveData<Int?> = ExposureSummaryRepository.daysSinceLastExposure
    val matchedKeyCount: LiveData<Int?> = ExposureSummaryRepository.matchedKeyCount

    // Values from TracingRepository
    val lastTimeDiagnosisKeysFetched: LiveData<Date> =
        TracingRepository.lastTimeDiagnosisKeysFetched
    val isTracingEnabled: LiveData<Boolean?> = TracingRepository.isTracingEnabled
    val activeTracingDaysInRetentionPeriod = TracingRepository.activeTracingDaysInRetentionPeriod
    var isRefreshing: LiveData<Boolean> = TracingRepository.isRefreshing

    // Todo exchange and get real stored risk level
    val savedRiskLevel = 2

    // Todo exchange and get the real next update date
    val nextUpdate = Date()

    /**
     * Launches the RiskLevelTransaction in the viewModel scope
     *
     * @see RiskLevelTransaction
     * @see RiskLevelRepository
     */
    fun refreshRiskLevel() {
        viewModelScope.launch {
            try {
                RiskLevelTransaction.start()
            } catch (e: TransactionException) {
                e.report(INTERNAL)
            }
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
                Log.v(TAG, "retrieved latest exposure summary from db")
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
}
