package de.rki.coronawarnapp.worker

import androidx.work.Constraints
import androidx.work.NetworkType

/**
 * Singleton class for background work helper functions
 * The helper uses externalised constants for readability.
 *
 * @see BackgroundConstants
 * @see BackgroundWorkScheduler
 */
object BackgroundWorkHelper {

    /**
     * Calculate the time for diagnosis key retrieval periodic work
     *
     * @return Long
     *
     * @see BackgroundConstants.MINUTES_IN_DAY
     * @see getDiagnosisKeyRetrievalMaximumCalls
     */
    fun getDiagnosisKeyRetrievalPeriodicWorkTimeInterval(): Long =
        (BackgroundConstants.MINUTES_IN_DAY / getDiagnosisKeyRetrievalMaximumCalls()).toLong()

    /**
     * Calculate the time for diagnosis key retrieval periodic work
     *
     * @return Long
     *
     * @see BackgroundConstants.MINUTES_IN_DAY
     */
    fun getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval(): Long =
        (BackgroundConstants.MINUTES_IN_DAY /
                BackgroundConstants.DIAGNOSIS_TEST_RESULT_RETRIEVAL_TRIES_PER_DAY).toLong()

    /**
     * Get maximum calls count to Google API
     *
     * @return Long
     *
     * @see BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_TRIES_PER_DAY
     * @see BackgroundConstants.GOOGLE_API_MAX_CALLS_PER_DAY
     */
    fun getDiagnosisKeyRetrievalMaximumCalls() =
        BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_TRIES_PER_DAY
            .coerceAtMost(BackgroundConstants.GOOGLE_API_MAX_CALLS_PER_DAY)

    /**
     * Constraints for diagnosis key one time work
     * Requires battery not low and any network connection
     * Mobile data usage is handled on OS level in application settings
     *
     * @return Constraints
     *
     * @see NetworkType.CONNECTED
     */
    fun getConstraintsForDiagnosisKeyOneTimeBackgroundWork() =
        Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
}
