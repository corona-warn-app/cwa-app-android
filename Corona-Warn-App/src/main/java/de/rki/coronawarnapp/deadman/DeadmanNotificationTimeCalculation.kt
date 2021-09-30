package de.rki.coronawarnapp.deadman

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.sortDateTime
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.DateTimeConstants
import org.joda.time.Instant
import org.joda.time.Minutes
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DeadmanNotificationTimeCalculation @Inject constructor(
    private val timeStamper: TimeStamper,
    private val keyCacheRepository: KeyCacheRepository,
) {

    /**
     * Get initial delay in minutes for deadman notification
     * If last success date time is null (eg: on application first start) - return [DEADMAN_NOTIFICATION_DELAY_MINUTES]
     */
    suspend fun getDelayInMinutes(): Long {
        val lastSuccess = keyCacheRepository.allCachedKeys()
            .first()
            .filter { it.info.isDownloadComplete }
            .maxByOrNull { it.info.sortDateTime }
            ?.info

        Timber.d("Last successful diagnosis key package download: $lastSuccess")
        return calculateDelay(lastSuccess?.createdAt).toLong()
    }

    /**
     * Calculate initial delay in minutes for deadman notification
     */
    internal fun calculateDelay(lastSuccess: Instant?): Int {
        val minutesSinceLastSuccess = if (lastSuccess != null)
            Minutes.minutesBetween(lastSuccess, timeStamper.nowUTC).minutes
        else 0
        return DEADMAN_NOTIFICATION_DELAY_MINUTES - minutesSinceLastSuccess
    }

    companion object {
        /**
         * Deadman notification background job delay set to 36 hours
         */
        const val DEADMAN_NOTIFICATION_DELAY_MINUTES = 36 * DateTimeConstants.MINUTES_PER_HOUR
    }
}
