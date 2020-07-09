package de.rki.coronawarnapp.risk

import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.tracing.TracingIntervalRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.daysToMilliseconds
import de.rki.coronawarnapp.util.TimeAndDateExtensions.roundUpMsToDays

object TimeVariables {

    /****************************************************
     * CONSTANTS
     ****************************************************/
    /**
     * Deactivation threshold time range
     * In seconds
     */
    private const val DEACTIVATION_TRACING_MEASURE_THRESHOLD_TIMERANGE = 60L

    /**
     * Getter function for [DEACTIVATION_TRACING_MEASURE_THRESHOLD_TIMERANGE]
     *
     * @return number of seconds
     */
    fun getDeactivationTracingMeasureThresholdTimeRange(): Long =
        DEACTIVATION_TRACING_MEASURE_THRESHOLD_TIMERANGE

    /**
     * The maximal runtime of a transaction
     * In milliseconds
     */
    private const val TRANSACTION_TIMEOUT = 60000L

    /**
     * Getter function for [TRANSACTION_TIMEOUT]
     *
     * @return timeout in milliseconds
     */
    fun getTransactionTimeout(): Long = TRANSACTION_TIMEOUT

    /**
     * The max timeRange for the exposure risk calculation.
     * In days.
     */
    private const val DEFAULT_RETENTION_PERIOD = 14

    /**
     * Getter function for [DEFAULT_RETENTION_PERIOD]
     *
     * @return max calculation range in days
     */
    fun getDefaultRetentionPeriodInDays() = DEFAULT_RETENTION_PERIOD

    /**
     * Getter function for [DEFAULT_RETENTION_PERIOD]
     *
     * @return max calculation range in ms
     */
    fun getDefaultRetentionPeriodInMS() =
        getDefaultRetentionPeriodInDays().toLong().daysToMilliseconds()

    /**
     * The time that the tracing has to be active to show the low risk level
     * In hours.
     */
    private const val MIN_ACTIVATED_TRACING_TIME = 24

    /**
     * Getter function for [MIN_ACTIVATED_TRACING_TIME]
     *
     * @return minimum required hours of active tracing
     */
    fun getMinActivatedTracingTime(): Int = MIN_ACTIVATED_TRACING_TIME

    /**
     * The timeRange until the calculated exposure figures are rated as stale.
     * In hours.
     */
    private const val MAX_STALE_EXPOSURE_RISK_RANGE = 48

    /**
     * Getter function for [MAX_STALE_EXPOSURE_RISK_RANGE]
     *
     * @return stale threshold in hours
     */
    fun getMaxStaleExposureRiskRange(): Int = MAX_STALE_EXPOSURE_RISK_RANGE

    private const val MILISECONDS_IN_A_SECOND = 1000
    private const val SECONDS_IN_A_MINUTES = 60
    private const val MINUTES_IN_AN_HOUR = 60
    private const val HOURS_IN_AN_DAY = 24

    /**
     * Delay in milliseconds for manual key retrieval process
     * Value for testing: 1 min =  1000 * 60 * 1
     * Value: 24 hours = 1000 * 60 * 60 * 24 milliseconds
     */
    private val MANUAL_KEY_RETRIEVAL_DELAY =
        if (BuildConfig.FLAVOR == "deviceForTesters") {
            MILISECONDS_IN_A_SECOND * SECONDS_IN_A_MINUTES
        } else {
            MILISECONDS_IN_A_SECOND * SECONDS_IN_A_MINUTES * MINUTES_IN_AN_HOUR * HOURS_IN_AN_DAY
        }

    /**
     * Getter function for [MANUAL_KEY_RETRIEVAL_DELAY]
     *
     * @return delay of key retrieval in milliseconds
     */
    fun getManualKeyRetrievalDelay() = MANUAL_KEY_RETRIEVAL_DELAY

    /**
     * This is the maximum attenuation duration value for the risk level calculation
     * in minutes
     */
    private const val MAX_ATTENUATION_DURATION = 30

    /**
     * Getter function for [MAX_ATTENUATION_DURATION]
     *
     * @return max attenuation duration in minutes
     */
    fun getMaxAttenuationDuration() = MAX_ATTENUATION_DURATION

    /****************************************************
     * STORED DATA
     ****************************************************/
    /**
     * timestamp when the tracing was activated by the user read from the mobile device storage.
     * The parameter is only filled once the tracing was activated and
     * not if the user activated or deactivates the tracing.
     *
     * It will change when you reinstall your app and activate tracing again.
     *
     * @return time in milliseconds when tracing was initially activated
     */
    fun getInitialExposureTracingActivationTimestamp(): Long? =
        LocalData.initialTracingActivationTimestamp()

    /**
     * timestamp when the last successful exposureRisk calculation happened read from the mobile device storage.
     * Last time when the transaction was successfully executed
     *
     * @return last time in milliseconds [de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction]
     * was run successfully
     */
    // because we have risk level calculation and key retrieval calculation
    fun getLastTimeDiagnosisKeysFromServerFetch(): Long? =
        LocalData.lastTimeDiagnosisKeysFromServerFetch()?.time

    /****************************************************
     * CALCULATED TIME VARIABLES
     ****************************************************/

    /**
     * The time since the last successful exposure calculation ran in foreground or background.
     * In milliseconds
     *
     * @return time in milliseconds since the exposure calculation was run successfully
     */
    fun getTimeSinceLastDiagnosisKeyFetchFromServer(): Long? {
        val lastTimeDiagnosisKeysFromServerFetch =
            getLastTimeDiagnosisKeysFromServerFetch() ?: return null
        return System.currentTimeMillis() - lastTimeDiagnosisKeysFromServerFetch
    }

    /**
     * The time the tracing is active.
     *
     * @return in milliseconds
     */
    fun getTimeActiveTracingDuration(): Long = System.currentTimeMillis() -
            (getInitialExposureTracingActivationTimestamp() ?: 0L) -
            LocalData.totalNonActiveTracing()

    suspend fun getActiveTracingDaysInRetentionPeriod(): Long {
        // the active tracing time during the retention period - all non active tracing times
        val tracingActiveMS = getTimeRangeFromRetentionPeriod()
        val inactiveTracingIntervals = TracingIntervalRepository
            .getDateRepository(CoronaWarnApplication.getAppContext())
            .getIntervals()
            .toMutableList()

        // by default the tracing is assumed to be activated
        // if the API is reachable we set the value accordingly 
        var enIsDisabled = false

        try {
            enIsDisabled = !InternalExposureNotificationClient.asyncIsEnabled()
        } catch (e: ApiException) {
            e.report(ExceptionCategory.EXPOSURENOTIFICATION)
        }

        if (enIsDisabled) {
            val current = System.currentTimeMillis()
            var lastTimeTracingWasNotActivated =
                LocalData.lastNonActiveTracingTimestamp() ?: current

            if (lastTimeTracingWasNotActivated < (current - getTimeRangeFromRetentionPeriod())) {
                lastTimeTracingWasNotActivated = current - getTimeRangeFromRetentionPeriod()
            }

            inactiveTracingIntervals.add(Pair(lastTimeTracingWasNotActivated, current))
        }

        val finalTracingMS = tracingActiveMS - inactiveTracingIntervals
            .map { it.second - it.first }
            .sum()

        return finalTracingMS.roundUpMsToDays()
    }

    /****************************************************
     * HELPER FUNCTIONS
     ****************************************************/

    /**
     * Return the maximum time of the time range that is used as retention time range.
     * The retention time range will be corrected to the initial exposure activation timestamp
     * (e.g. when we reset our data or start tracing for the first time after a fresh install)
     *
     * @return max number of days the server should fetch
     */
    private fun getTimeRangeFromRetentionPeriod(): Long {
        val activeTracingTimeInMS =
            getInitialExposureTracingActivationTimestamp()?.let {
                System.currentTimeMillis().minus(it)
            } ?: return 0

        return if (activeTracingTimeInMS > getDefaultRetentionPeriodInMS()) {
            getDefaultRetentionPeriodInMS()
        } else {
            activeTracingTimeInMS
        }
    }
}
