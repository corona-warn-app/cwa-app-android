package de.rki.coronawarnapp.reyclebin.coronatest

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.errors.CoronaTestNotFoundException
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RecycledCoronaTestsProvider @Inject constructor(
    private val coronaTestRepository: CoronaTestRepository,
) {

    val tests: Flow<Set<CoronaTest>> = coronaTestRepository.recycledCoronaTests

    /**
     * Find corona test in recycled items
     * @return [CoronaTest] if found , otherwise `null`
     */
    suspend fun findCoronaTest(coronaTestQrCodeHash: String?): CoronaTest? {
        if (coronaTestQrCodeHash == null) return null
        Timber.tag(TAG).d("findCoronaTest(coronaTestQrCodeHash=%s)", coronaTestQrCodeHash)
        return tests.first()
            .find { it.qrCodeHash == coronaTestQrCodeHash }
            .also { Timber.tag(TAG).d("returning %s", it) }
    }

    suspend fun recycleCoronaTest(identifier: TestIdentifier) {
        Timber.tag(TAG).d("recycleCoronaTest(identifier=%s)", identifier)
        coronaTestRepository.recycleTest(identifier)
    }

    suspend fun restoreCoronaTest(identifier: TestIdentifier) {
        Timber.tag(TAG).d("restoreCoronaTest(identifier=%s)", identifier)
        coronaTestRepository.restoreTest(identifier)
    }

    suspend fun deleteCoronaTest(identifier: TestIdentifier) {
        try {
            Timber.tag(TAG).d("deleteCoronaTest(identifier=%s)", identifier)
            coronaTestRepository.removeTest(identifier)
        } catch (e: CoronaTestNotFoundException) {
            Timber.tag(TAG).e(e)
        }
    }

    suspend fun deleteAllCoronaTest(identifiers: Collection<TestIdentifier>) {
        Timber.tag(TAG).d("deleteAllCoronaTest(identifiers=%s)", identifiers)
        identifiers
            .toSet()
            .forEach { deleteCoronaTest(identifier = it) }
    }

    companion object {
        private val TAG = tag<RecycledCoronaTestsProvider>()
    }
}
