package de.rki.coronawarnapp.reyclebin.coronatest

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.errors.CoronaTestNotFoundException
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecycledCoronaTestsRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val coronaTestRepository: CoronaTestRepository,
    private val recycledCoronaTestsStorage: RecycledCoronaTestsStorage,
    private val timeStamper: TimeStamper
) {

    private val appScopeIO: CoroutineScope
        get() = appScope + dispatcherProvider.IO

    private val internalData: HotDataFlow<Set<RecycledCoronaTest>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScopeIO,
        sharingBehavior = SharingStarted.Lazily,
        startValueProvider = {
            recycledCoronaTestsStorage.load()
                .also { Timber.tag(TAG).d("Restored recycledTests=%s", it) }
        }
    )

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing recycled tests") }
            .drop(1) // Skip restored data
            .onEach {
                Timber.tag(TAG).v("Recycled test changed: %s", it)
                recycledCoronaTestsStorage.save(it)
            }
            .catch {
                it.reportProblem(TAG, "Storing recycled tests failed")
                throw it
            }
            .launchIn(scope = appScopeIO)
    }

    val tests: Flow<Set<RecycledCoronaTest>> = internalData.data

    /**
     * Find corona test in recycled items
     * @return [RecycledCoronaTest] if found , otherwise `null`
     */
    suspend fun findCoronaTest(coronaTestQrCodeHash: String): RecycledCoronaTest? {
        Timber.tag(TAG).d("findCoronaTest(coronaTestQrCodeHash=%s)", coronaTestQrCodeHash)
        return internalData.data.first()
            .find { it.coronaTest.qrCodeHash == coronaTestQrCodeHash }
            .also { Timber.tag(TAG).d("returning %s", it) }
    }

    suspend fun addCoronaTest(coronaTest: CoronaTest) {
        Timber.tag(TAG).d("addCoronaTest(coronaTest=%s)", coronaTest)
        val now = timeStamper.nowUTC
        internalData.updateBlocking {
            try {
                val testToRecycle = coronaTestRepository.removeTest(coronaTest.identifier)
                    .toRecycledCoronaTest(recycledAt = now)
                Timber.d("Adding %s to recycled tests", testToRecycle)
                this.plus(testToRecycle)
            } catch (e: CoronaTestNotFoundException) {
                Timber.tag(TAG).e(e, "Failed to recycle test=%s", coronaTest)
                this
            }
        }
    }

    suspend fun restoreCoronaTest(recycledCoronaTest: RecycledCoronaTest) {
        Timber.tag(TAG).d("restoreCoronaTest(recycledCoronaTest=%s)", recycledCoronaTest)
        coronaTestRepository.restoreTest(recycledCoronaTest.coronaTest)
        deleteCoronaTest(recycledCoronaTest)
    }

    suspend fun deleteCoronaTest(recycledCoronaTest: RecycledCoronaTest) {
        Timber.tag(TAG).d("deleteCoronaTest(recycledCoronaTest=%s)", recycledCoronaTest)
        internalData.updateBlocking {
            Timber.d("Deleting %s", recycledCoronaTest)
            this.minus(recycledCoronaTest)
        }
    }

    suspend fun deleteAllCoronaTest(recycledCoronaTests: Collection<RecycledCoronaTest>) {
        Timber.tag(TAG).d("deleteAllCoronaTest(recycledCoronaTests=%s)", recycledCoronaTests)
        internalData.updateBlocking {
            Timber.d("Deleting %s", recycledCoronaTests)
            this.minus(recycledCoronaTests)
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).d("clear()")
        internalData.updateBlocking {
            Timber.tag(TAG).d("Deleting %s", this)
            emptySet()
        }
    }

    companion object {
        private val TAG = tag<RecycledCoronaTestsRepository>()
    }
}
