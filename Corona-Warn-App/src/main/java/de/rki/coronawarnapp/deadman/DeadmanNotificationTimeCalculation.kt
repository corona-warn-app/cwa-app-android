package de.rki.coronawarnapp.deadman

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.sortDateTime
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppInstallTime
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class DeadmanNotificationTimeCalculation @Inject constructor(
    private val timeStamper: TimeStamper,
    private val keyCacheRepository: KeyCacheRepository,
    @AppInstallTime private val installTime: Instant
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
        Timber.d("Install time=%s", installTime)
        return calculateDelay(lastSuccess?.createdAt ?: installTime)
    }

    /**
     * Calculate initial delay in minutes for deadman notification
     */
    internal fun calculateDelay(lastSuccess: Instant): Long {
        val minutesSinceLastSuccess = Duration.between(lastSuccess, timeStamper.nowUTC).toMinutes()
        return DEADMAN_NOTIFICATION_DELAY_MINUTES - minutesSinceLastSuccess
    }

    companion object {
        /**
         * Deadman notification background job delay set to 36 hours
         */
        val DEADMAN_NOTIFICATION_DELAY_MINUTES = 36 * TimeUnit.HOURS.toMinutes(1)
    }
}
