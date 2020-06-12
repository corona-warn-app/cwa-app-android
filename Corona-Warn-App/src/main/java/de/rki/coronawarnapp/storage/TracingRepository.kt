package de.rki.coronawarnapp.storage

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.TimeVariables.getActiveTracingDaysInRetentionPeriod
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import java.util.Date

/**
 * The Tracing Repository refreshes and triggers all tracing relevant data. Some functions get their
 * data directly from the Exposure Notification, others consume the shared preferences.
 *
 * @see LocalData
 * @see InternalExposureNotificationClient
 * @see RetrieveDiagnosisKeysTransaction
 * @see RiskLevelRepository
 */
object TracingRepository {

    private val TAG: String? = TracingRepository::class.simpleName

    // public mutable live data
    val lastTimeDiagnosisKeysFetched = MutableLiveData<Date>()
    val isTracingEnabled = MutableLiveData<Boolean>()
    val isRefreshing = MutableLiveData(false)
    val activeTracingDaysInRetentionPeriod = MutableLiveData<Long>()

    /**
     * Refresh the last time diagnosis keys fetched date with the current shared preferences state.
     *
     * @see LocalData
     */
    fun refreshLastTimeDiagnosisKeysFetchedDate() {
        lastTimeDiagnosisKeysFetched.postValue(LocalData.lastTimeDiagnosisKeysFromServerFetch())
    }

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
    suspend fun refreshDiagnosisKeys() {
        isRefreshing.value = true
        try {
            RetrieveDiagnosisKeysTransaction.start()
            RiskLevelTransaction.start()
        } catch (e: TransactionException) {
            e.cause?.report(ExceptionCategory.EXPOSURENOTIFICATION)
        } catch (e: Exception) {
            e.report(ExceptionCategory.EXPOSURENOTIFICATION)
        }
        refreshLastTimeDiagnosisKeysFetchedDate()
        isRefreshing.value = false
    }

    /**
     * Get the current tracing status from the Exposure Notification API.
     *
     * @see InternalExposureNotificationClient
     */
    suspend fun refreshIsTracingEnabled() {
        try {
            val isEnabled = InternalExposureNotificationClient.asyncIsEnabled()
            isTracingEnabled.value = isEnabled
        } catch (e: Exception) {
            // when API is not available, ensure tracing is displayed as off
            isTracingEnabled.postValue(false)
            e.report(
                ExceptionCategory.EXPOSURENOTIFICATION,
                TAG,
                null
            )
        }
    }

    /**
     * Refresh the activeTracingDaysInRetentionPeriod calculation.
     *
     * @see de.rki.coronawarnapp.risk.TimeVariables
     */
    suspend fun refreshActiveTracingDaysInRetentionPeriod() {
        activeTracingDaysInRetentionPeriod.postValue(getActiveTracingDaysInRetentionPeriod())
    }
}
