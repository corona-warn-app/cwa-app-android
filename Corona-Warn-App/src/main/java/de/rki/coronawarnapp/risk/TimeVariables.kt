package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.TimeAndDateExtensions.daysToMilliseconds

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
     * Stay below 10min with this timeout!
     * We only 10min background execution time via WorkManager.
     */
    private const val TRANSACTION_TIMEOUT = 8 * 60 * 1000L

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

    private const val MILLISECONDS_IN_A_SECOND = 1000
    private const val SECONDS_IN_A_MINUTES = 60
    private const val MINUTES_IN_AN_HOUR = 60
    private const val HOURS_IN_AN_DAY = 24

    /**
     * Delay in milliseconds for manual key retrieval process
     * Value for testing: 1 min =  1000 * 60 * 1
     * Value: 24 hours = 1000 * 60 * 60 * 24 milliseconds
     *
     * @return delay of key retrieval in milliseconds
     */
    fun getManualKeyRetrievalDelay() =
        if (CWADebug.buildFlavor == CWADebug.BuildFlavor.DEVICE_FOR_TESTERS) {
            MILLISECONDS_IN_A_SECOND * SECONDS_IN_A_MINUTES
        } else {
            MILLISECONDS_IN_A_SECOND * SECONDS_IN_A_MINUTES * MINUTES_IN_AN_HOUR * HOURS_IN_AN_DAY
        }

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

    /****************************************************
     * CALCULATED TIME VARIABLES
     ****************************************************/

    /**
     * The time the tracing is active.
     *
     * @return in milliseconds
     */
    fun getTimeActiveTracingDuration(): Long = System.currentTimeMillis() -
        (getInitialExposureTracingActivationTimestamp() ?: 0L) -
        LocalData.totalNonActiveTracing()

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
