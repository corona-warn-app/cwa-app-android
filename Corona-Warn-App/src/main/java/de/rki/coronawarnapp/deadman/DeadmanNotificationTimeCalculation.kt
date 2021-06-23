package de.rki.coronawarnapp.deadman

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.pkgDateTime
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
     * If last success date time is null (eg: on application first start) - return [DEADMAN_NOTIFICATION_DELAY_IN_HOURS]
     */
    suspend fun getDelay(): Long {
        val latestKeyPackageInfo = keyCacheRepository.allCachedKeys()
            .first()
            .filter { it.info.isDownloadComplete }
            .maxByOrNull { it.info.pkgDateTime }
            ?.info

        Timber.d("Last successful diagnosis key package download: $latestKeyPackageInfo")
        return if (latestKeyPackageInfo != null) {
            calculateDelayInMinutes(latestKeyPackageInfo.pkgDateTime.toInstant()).toLong()
        } else {
            DEADMAN_NOTIFICATION_DELAY_IN_MINUTES.toLong()
        }
    }

    /**
     * Calculate initial delay in minutes for deadman notification
     */
    internal fun calculateDelayInMinutes(lastSuccess: Instant): Int =
        DEADMAN_NOTIFICATION_DELAY_IN_MINUTES - Minutes.minutesBetween(lastSuccess, timeStamper.nowUTC).minutes

    companion object {
        /**
         * Deadman notification background job delay set to 36 hours
         */
        const val DEADMAN_NOTIFICATION_DELAY_IN_MINUTES = 36 * DateTimeConstants.MINUTES_PER_HOUR
    }
}
