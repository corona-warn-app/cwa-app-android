package de.rki.coronawarnapp.deadman

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.pkgDateTime
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.DateTimeConstants
import org.joda.time.Hours
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DeadmanNotificationTimeCalculation @Inject constructor(
    private val timeStamper: TimeStamper,
    private val keyCacheRepository: KeyCacheRepository,
) {

    /**
     * Calculate initial delay in minutes for deadman notification
     */
    fun getHoursDiff(lastSuccess: Instant): Int {
        val hoursDiff = Hours.hoursBetween(lastSuccess, timeStamper.nowUTC)
        return (DEADMAN_NOTIFICATION_DELAY - hoursDiff.hours) * DateTimeConstants.MINUTES_PER_HOUR
    }

    /**
     * Get initial delay in minutes for deadman notification
     * If last success date time is null (eg: on application first start) - return [DEADMAN_NOTIFICATION_DELAY]
     */
    suspend fun getDelay(): Long {
        val lastSuccess = keyCacheRepository.allCachedKeys()
            .first()
            .filter { it.info.isDownloadComplete }
            .maxByOrNull { it.info.pkgDateTime }
            ?.info

        Timber.d("Last successful diagnosis key package download: $lastSuccess")
        return if (lastSuccess != null) {
            getHoursDiff(lastSuccess.createdAt).toLong()
        } else {
            (DEADMAN_NOTIFICATION_DELAY * DateTimeConstants.MINUTES_PER_HOUR).toLong()
        }
    }

    companion object {
        /**
         * Deadman notification background job delay set to 36 hours
         */
        const val DEADMAN_NOTIFICATION_DELAY = 36
    }
}
