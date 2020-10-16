package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.TimeVariables.getActiveTracingDaysInRetentionPeriod
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    // TODO shouldn't access this directly
    val internalIsRefreshing = MutableStateFlow(false)
    val isRefreshing: Flow<Boolean> = internalIsRefreshing

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
        internalIsRefreshing.value = true
        try {
            RetrieveDiagnosisKeysTransaction.start()
            RiskLevelTransaction.start()
        } catch (e: TransactionException) {
            e.cause?.report(ExceptionCategory.EXPOSURENOTIFICATION)
        } catch (e: Exception) {
            e.report(ExceptionCategory.EXPOSURENOTIFICATION)
        }
        refreshLastTimeDiagnosisKeysFetchedDate()
        internalIsRefreshing.value = false
    }

    /**
     * Refresh the activeTracingDaysInRetentionPeriod calculation.
     *
     * @see de.rki.coronawarnapp.risk.TimeVariables
     */
    suspend fun refreshActiveTracingDaysInRetentionPeriod() {
        internalActiveTracingDaysInRetentionPeriod.value = getActiveTracingDaysInRetentionPeriod()
    }
}
