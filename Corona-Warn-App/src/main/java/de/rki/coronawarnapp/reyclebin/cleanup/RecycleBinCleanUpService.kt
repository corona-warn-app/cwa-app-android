package de.rki.coronawarnapp.reyclebin.cleanup

import dagger.Reusable
import de.rki.coronawarnapp.reyclebin.common.Recyclable
import de.rki.coronawarnapp.reyclebin.common.Recyclable.Companion.RETENTION_DAYS
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.reyclebin.common.retentionTimeInRecycleBin
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.Instant
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

        deleteRecycledCerts(now)
        deleteRecycledCoronaTests(now)

        Timber.tag(TAG).d("clearRecycledItems() - Finished")
    }

    private suspend fun allRecycledCerts() = recycledCertificatesProvider.recycledCertificates.first()
        .also { Timber.tag(TAG).d("allRecycledCerts=%s", it) }

    private suspend fun allRecycledCoronaTests() = recycledCoronaTestsProvider.tests.first()
        .also { Timber.tag(TAG).d("allRecycledCoronaTests=%s", it) }

    private suspend fun deleteRecycledCerts(now: Instant) {
        Timber.tag(TAG).d("deleteRecycledCerts()")
        allRecycledCerts().deleteRecycledItemsOutOfRetention(now) { items ->
            val containerIds = items.map { it.containerId }
            recycledCertificatesProvider.deleteAllCertificate(containerIds)
        }
    }

    private suspend fun deleteRecycledCoronaTests(now: Instant) {
        Timber.tag(TAG).d("deleteRecycledCoronaTests()")
        allRecycledCoronaTests().deleteRecycledItemsOutOfRetention(now = now) { items ->
            val identifiers = items.map { it.identifier }
            recycledCoronaTestsProvider.deleteAllCoronaTest(identifiers)
        }
    }

    private inline fun <reified T : Recyclable> Collection<T>.deleteRecycledItemsOutOfRetention(
        now: Instant,
        deleteAction: (Collection<T>) -> Unit
    ) {
        val recycledItemsExceededRetentionDays = filter {
            it.retentionTimeInRecycleBin(now = now) > RETENTION_DAYS
        }.also { Timber.tag(TAG).d("recycledItemsExceededRetentionDays=%s", it) }

        if (recycledItemsExceededRetentionDays.isEmpty()) {
            Timber.tag(TAG).d(
                message = "No recycled item exceeded the retention time of %d days, returning early",
                RETENTION_DAYS.toDays()
            )
            return
        }

        deleteAction(recycledItemsExceededRetentionDays)
    }

    companion object {
        private val TAG = tag<RecycleBinCleanUpService>()
    }
}
