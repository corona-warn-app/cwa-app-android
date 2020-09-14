package de.rki.coronawarnapp.worker

import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.storage.LocalData
import kotlin.random.Random

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
     * Get background noise one time work delay
     * The periodic job is already delayed by MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION
     * so we only need to delay further by the difference between min and max.
     *
     * @return Long
     *
     * @see BackgroundConstants.MAX_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION
     * @see BackgroundConstants.MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION
     */
    fun getBackgroundNoiseOneTimeWorkDelay() = Random.nextLong(
        0,
        BackgroundConstants.MAX_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION -
                BackgroundConstants.MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION
    )

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

    /**
     * Send debug notification to check background jobs execution
     *
     * @param title: String
     * @param content: String
     *
     * @see LocalData.backgroundNotification()
     */
    fun sendDebugNotification(title: String, content: String) {
        if (!LocalData.backgroundNotification()) return
        NotificationHelper.sendNotification(title, content, NotificationCompat.PRIORITY_HIGH, true)
    }
}
