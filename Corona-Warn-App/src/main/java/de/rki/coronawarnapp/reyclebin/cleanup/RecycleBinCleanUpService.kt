package de.rki.coronawarnapp.reyclebin.cleanup

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.reyclebin.common.Recyclable
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.reyclebin.common.retentionTimeInRecycleBin
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
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
    private val recycledCoronaTestsProvider: RecycledCoronaTestsProvider,
    private val timeStamper: TimeStamper
) {

    private val mutex = Mutex()

    suspend fun clearRecycledItems() = mutex.withLock {
        Timber.tag(TAG).d("clearRecycledItems() - Started")


        val now = timeStamper.nowUTC
        Timber.tag(TAG).d("now=%s", now)

        val allRecycledItems: Set<Recyclable> = allRecycledCerts() + allRecycledCoronaTests()
        Timber.tag(TAG).d("allRecycledItems=%s", allRecycledItems)

        val recycledItemsExceededRetentionDays = allRecycledItems
            .filter { it.retentionTimeInRecycleBin(now = now) > RETENTION_DAYS }
            .also { Timber.tag(TAG).d("recycledItemsExceededRetentionDays=%s", it) }

        if (recycledItemsExceededRetentionDays.isEmpty()) {
            Timber.tag(TAG).d(
                message = "No recycled item exceeded the retention time of %d days, returning early",
                RETENTION_DAYS.standardDays
            )
            return
        }

        recycledItemsExceededRetentionDays
            .filterIsInstance<CwaCovidCertificate>()
            .deleteRecycledCerts()

        recycledItemsExceededRetentionDays
            .filterIsInstance<CoronaTest>()
            .deleteRecycledCoronaTests()

        Timber.tag(TAG).d("clearRecycledItems() - Finished")
    }

    private suspend fun allRecycledCerts() = recycledCertificatesProvider.recycledCertificates.first()
        .also { Timber.tag(TAG).d("allRecycledCerts=%s", it) }

    private suspend fun allRecycledCoronaTests() = recycledCoronaTestsProvider.tests.first()
        .also { Timber.tag(TAG).d("allRecycledCoronaTests=%s", it) }

    private suspend fun Collection<CwaCovidCertificate>.deleteRecycledCerts() {
        Timber.tag(TAG).d("deleteRecycledCerts=%s", this)
        val containerIds = map { it.containerId }
        recycledCertificatesProvider.deleteAllCertificate(containerIds)
    }

    private suspend fun Collection<CoronaTest>.deleteRecycledCoronaTests() {
        Timber.tag(TAG).d("deleteRecycledCoronaTests=%s", this)
        val identifiers = map { it.identifier }
        recycledCoronaTestsProvider.deleteAllCoronaTest(identifiers)
    }

    companion object {
        private val TAG = tag<RecycleBinCleanUpService>()

        @VisibleForTesting
        val RETENTION_DAYS: Duration = Days.days(30).toStandardDuration()
    }
}
