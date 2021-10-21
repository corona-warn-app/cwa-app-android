package de.rki.coronawarnapp.reyclebin.cleanup

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.reyclebin.common.retentionTimeInRecycleBin
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsRepository
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.Days
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RecycleBinCleanUpService @Inject constructor(
    private val recycledCertificatesProvider: RecycledCertificatesProvider,
    private val recycledCoronaTestsRepository: RecycledCoronaTestsRepository,
    private val timeStamper: TimeStamper
) {

    private val mutex = Mutex()

    suspend fun clearRecycledCertificates() = mutex.withLock {
        Timber.tag(TAG).d("clearRecycledCertificates() - Started")

        val now = timeStamper.nowUTC
        Timber.tag(TAG).d("now=%s", now)

        val allRecycledCerts = recycledCertificatesProvider.recycledCertificates.first()
        Timber.tag(TAG).d("allRecycledCerts=%s", allRecycledCerts)

        val recycledCertsExceededRetentionDays = allRecycledCerts
            .filter { it.retentionTimeInRecycleBin(now = now) > RETENTION_DAYS }
        Timber.tag(TAG).d("recycledCertsExceededRetentionDays=%s", recycledCertsExceededRetentionDays)

        if (recycledCertsExceededRetentionDays.isEmpty()) {
            Timber.tag(TAG).d(
                message = "No recycled cert exceeded the retention time of %d days, returning early",
                RETENTION_DAYS.standardDays
            )
            return
        }

        // TODO clean up outdated recycled tests

        recycledCertsExceededRetentionDays.map { it.containerId }
            .also { recycledCertificatesProvider.deleteAllCertificate(it) }

        Timber.tag(TAG).d("clearRecycledCertificates() - Finished")
    }

    companion object {
        private val TAG = tag<RecycleBinCleanUpService>()

        @VisibleForTesting
        val RETENTION_DAYS: Duration = Days.days(30).toStandardDuration()
    }
}
