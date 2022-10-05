package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.nearby.ENFClientLocalData
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.Duration
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmissionQuota @Inject constructor(
    private val enfData: ENFClientLocalData,
    private val timeStamper: TimeStamper
) {

    private val mutex = Mutex()

    /**
     * Attempts to consume quota, and returns true if enough quota was available.
     */
    suspend fun consumeQuota(wanted: Int): Boolean = mutex.withLock {
        attemptQuotaReset()

        val currentQuota = enfData.currentQuota.first()
        if (currentQuota < wanted) {
            Timber.d("Not enough quota: want=%d, have=%d", wanted, enfData.currentQuota.first())
            return false
        }

        run {
            val oldQuota = currentQuota
            val newQuota = currentQuota - wanted
            Timber.d("Consuming quota: old=%d, new=%d", oldQuota, newQuota)
            enfData.updateCurrentQuota(newQuota)
        }
        return true
    }

    /**
     * Attempts to reset the quota
     * On initial launch, the lastQuotaReset is set to Instant.EPOCH,
     * thus the quota will be immediately set to 20.
     */
    private suspend fun attemptQuotaReset() {
        val oldQuota = enfData.currentQuota.first()
        val oldQuotaReset = enfData.lastQuotaResetAt.first()

        val now = timeStamper.nowUTC

        val nextQuotaReset = enfData.lastQuotaResetAt.first()
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .atStartOfDay()
            .atZone(ZoneOffset.UTC)
            .plus(Duration.ofDays(1))
            .toInstant()

        if (now.isAfter(nextQuotaReset)) {
            enfData.updateCurrentQuota(DEFAULT_QUOTA)
            enfData.updateLastQuotaResetAt(now)

            Timber.i(
                "Quota reset: oldQuota=%d, lastReset=%s -> newQuota=%d, thisReset=%s",
                oldQuota,
                oldQuotaReset,
                enfData.currentQuota.first(),
                now
            )
        } else {
            Timber.d(
                "No new quota available (now=%s, availableAt=%s)",
                now,
                nextQuotaReset
            )
        }
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        /**
         * This quota should be 6 when using ExposureWindow
         * See: https://developers.google.com/android/exposure-notifications/release-notes
         */
        internal const val DEFAULT_QUOTA = 6
    }
}
