package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.nearby.ENFClientLocalData
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmissionQuota @Inject constructor(
    private val enfData: ENFClientLocalData,
    private val timeStamper: TimeStamper
) {

    private var currentQuota: Int
        get() = enfData.currentQuota
        set(value) {
            enfData.currentQuota = value
        }

    private var lastQuotaReset: Instant
        get() = enfData.lastQuotaResetAt
        set(value) {
            enfData.lastQuotaResetAt = value
        }

    private val mutex = Mutex()

    /**
     * Attempts to consume quota, and returns true if enough quota was available.
     */
    suspend fun consumeQuota(wanted: Int): Boolean = mutex.withLock {
        attemptQuotaReset()

        if (currentQuota < wanted) {
            Timber.tag(TAG).d("Not enough quota: want=%d, have=%d", wanted, currentQuota)
            return false
        }

        run {
            val oldQuota = currentQuota
            val newQuota = currentQuota - wanted
            Timber.tag(TAG).d("Consuming quota: old=%d, new=%d", oldQuota, newQuota)
            currentQuota = newQuota
        }
        return true
    }

    /**
     * Attempts to reset the quota
     * On initial launch, the lastQuotaReset is set to Instant.EPOCH,
     * thus the quota will be immediately set to 20.
     */
    private fun attemptQuotaReset() {
        val oldQuota = currentQuota
        val oldQuotaReset = lastQuotaReset

        val now = timeStamper.nowUTC

        val nextQuotaReset = lastQuotaReset
            .toDateTime(DateTimeZone.UTC)
            .withTimeAtStartOfDay()
            .plus(Duration.standardDays(1))

        if (now.isAfter(nextQuotaReset)) {
            currentQuota = DEFAULT_QUOTA
            lastQuotaReset = now

            Timber.tag(TAG).i(
                "Quota reset: oldQuota=%d, lastReset=%s -> newQuota=%d, thisReset=%s",
                oldQuota, oldQuotaReset, currentQuota, now
            )
        } else {
            Timber.tag(TAG).d(
                "No new quota available (now=%s, availableAt=%s)",
                now, nextQuotaReset
            )
        }
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal const val DEFAULT_QUOTA = 20

        private val TAG: String = SubmissionQuota::class.java.simpleName
    }
}
