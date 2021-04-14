package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.migration.CoronaTestMigration
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.storage.CoronaTestStorage
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoronaTestRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val storage: CoronaTestStorage,
    private val processors: Set<@JvmSuppressWildcards CoronaTestProcessor>,
    private val legacyMigration: CoronaTestMigration,
) {

    private val internalData: HotDataFlow<Map<CoronaTestGUID, CoronaTest>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Eagerly,
    ) {
        val legacyTests = legacyMigration.startMigration()
        (legacyTests + storage.coronaTests).map {
            it.testGUID to it
        }.toMap()
    }

    val coronaTests: Flow<Set<CoronaTest>> = internalData.data.map { it.values.toSet() }

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing test data.") }
            .onEach {
                Timber.tag(TAG).v("CoronaTest data changed: %s", it)
                storage.coronaTests = it.values.toSet()
                legacyMigration.finishMigration()
            }
            .catch { it.reportProblem(TAG, "Failed to snapshot CoronaTest data to storage.") }
            .launchIn(appScope + dispatcherProvider.IO)
    }

    private fun getProcessor(type: CoronaTest.Type) = processors.single { it.type == type }

    /**
     * When this returns and there was no exception, the test was registered and a valid registrationToken obtained.
     * Your new test should be available via **coronaTests**.
     */
    suspend fun registerTest(request: CoronaTestQRCode) {
        Timber.tag(TAG).i("registerTest(request=%s)", request)
        // We check early, if there is no processor, crash early, "should" never happen though...
        val processor = getProcessor(request.type)

        internalData.updateBlocking {
            if (values.any { it.type == request.type }) {
                throw IllegalStateException("There is already a test of this type: ${request.type}.")
            }

            val newCoronaTest = processor.create(request)
            Timber.tag(TAG).i("Adding new test: %s", newCoronaTest)

            toMutableMap().apply {
                this[newCoronaTest.testGUID] = newCoronaTest
            }
        }
    }

    suspend fun removeTest(guid: CoronaTestGUID): CoronaTest {
        Timber.tag(TAG).i("removeTest(guid=%s)", guid)
        var removedTest: CoronaTest? = null

        internalData.updateBlocking {
            if (!containsKey(guid)) throw IllegalStateException("No test with guid $guid is known.")

            val toBeRemoved = getValue(guid)
            getProcessor(toBeRemoved.type).onRemove(toBeRemoved)

            toMutableMap().apply {
                removedTest = remove(guid)
                Timber.tag(TAG).d("Removed: %s", removedTest)
            }
        }

        return removedTest!!
    }

    /**
     * Passing **null** will refresh all test types.
     */
    suspend fun refresh(type: CoronaTest.Type? = null) {
        Timber.tag(TAG).d("refresh(type=%s)", type)

        val toRefreshGUIDs = internalData.data
            .first().values
            .filter { if (type == null) true else it.type == type }
            .map { it.testGUID }

        Timber.tag(TAG).d("Will refresh %s", toRefreshGUIDs)

        toRefreshGUIDs.forEach {
            modifyTest(it) { processor, test ->
                processor.markProcessing(test, true)
            }
        }

        internalData.updateBlocking {
            val polling = values
                .filter { if (type == null) true else it.type == type }
                .filter { toRefreshGUIDs.contains(it.testGUID) }
                .map { coronaTest ->

                    withContext(context = dispatcherProvider.IO) {
                        async {
                            Timber.tag(TAG).v("Polling for %s", coronaTest)
                            // This will not throw an exception
                            // Any error encountered during polling will be in CoronaTest.lastError
                            getProcessor(coronaTest.type).pollServer(coronaTest)
                        }
                    }
                }

            Timber.tag(TAG).d("Waiting for test status polling: %s", polling)
            val pollingResults = polling.awaitAll().filterNotNull()

            this.toMutableMap().apply {
                for (updatedResult in pollingResults) {
                    this[updatedResult.testGUID] = updatedResult
                }
            }
        }

        toRefreshGUIDs.forEach {
            modifyTest(it) { processor, test ->
                processor.markProcessing(test, false)
            }
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).i("clear()")
        internalData.updateBlocking {
            Timber.tag(TAG).d("Clearing %s", this)
            emptyMap()
        }
    }

    suspend fun markAsSubmitted(guid: CoronaTestGUID) {
        Timber.tag(TAG).i("markAsSubmitted(guid=%s)", guid)

        modifyTest(guid) { processor, before ->
            processor.markSubmitted(before)
        }
    }

    private suspend fun modifyTest(
        guid: CoronaTestGUID,
        update: suspend (CoronaTestProcessor, CoronaTest) -> CoronaTest
    ) {
        internalData.updateBlocking {
            if (!containsKey(guid)) throw IllegalStateException("No test with guid $guid is known.")

            val original = getValue(guid)
            val processor = getProcessor(original.type)

            val updated = update(processor, original)
            Timber.tag(TAG).d("Updated %s to %s", original, updated)

            toMutableMap().apply { this[guid] = updated }
        }
    }

    companion object {
        const val TAG = "CoronaTestRepository"
    }
}
