package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.errors.CoronaTestNotFoundException
import de.rki.coronawarnapp.coronatest.errors.DuplicateCoronaTestException
import de.rki.coronawarnapp.coronatest.errors.UnknownTestTypeException
import de.rki.coronawarnapp.coronatest.migration.PCRTestMigration
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.storage.CoronaTestStorage
import de.rki.coronawarnapp.coronatestjournal.storage.TestResultStorage
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
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
    private val legacyMigration: PCRTestMigration,
    private val testResultStorage: TestResultStorage
) {

    private val internalData: HotDataFlow<Map<CoronaTestGUID, CoronaTest>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Eagerly,
    ) {
        val legacyTests = legacyMigration.startMigration()
        val persistedTests = storage.coronaTests
        (legacyTests + persistedTests).map { it.identifier to it }.toMap().also {
            Timber.tag(TAG).v("Restored CoronaTest data: %s", it)
        }
    }

    val coronaTests: Flow<Set<CoronaTest>> = internalData.data.map { it.values.toSet() }

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing test data.") }
            .onEach {
                Timber.tag(TAG).v("CoronaTest data changed: %s", it)
                storage.coronaTests = it.values.toSet()
                legacyMigration.finishMigration()
                testResultStorage.updateResults(it)
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot CoronaTest data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.IO)
    }

    private fun getProcessor(type: CoronaTest.Type) = processors.single { it.type == type }

    suspend fun registerTest(request: TestRegistrationRequest): CoronaTest {
        Timber.tag(TAG).i("registerTest(request=%s)", request)

        // We check early, if there is no processor, crash early, "should" never happen though...
        val processor = getProcessor(request.type)

        val currentTests = internalData.updateBlocking {
            if (values.any { it.type == request.type }) {
                throw DuplicateCoronaTestException("There is already a test of this type: ${request.type}.")
            }

            val test = when (request) {
                is CoronaTestQRCode -> processor.create(request)
                is CoronaTestTAN -> processor.create(request)
                else -> throw UnknownTestTypeException("Unknown test request: $request")
            }

            Timber.tag(TAG).i("Adding new test: %s", test)

            toMutableMap().apply { this[test.identifier] = test }
        }

        return currentTests[request.identifier]!!
    }

    suspend fun removeTest(identifier: TestIdentifier): CoronaTest {
        Timber.tag(TAG).i("removeTest(identifier=%s)", identifier)

        var removedTest: CoronaTest? = null

        internalData.updateBlocking {
            val toBeRemoved = values.singleOrNull { it.identifier == identifier }
                ?: throw CoronaTestNotFoundException("No found for $identifier")

            getProcessor(toBeRemoved.type).onRemove(toBeRemoved)

            toMutableMap().apply {
                removedTest = remove(toBeRemoved.identifier)
                Timber.tag(TAG).d("Removed: %s", removedTest)
            }
        }

        return removedTest!!
    }

    /**
     * Passing **null** will refresh all test types.
     */
    suspend fun refresh(type: CoronaTest.Type? = null): Set<CoronaTest> {
        Timber.tag(TAG).d("refresh(type=%s)", type)

        val toRefresh = internalData.data
            .first().values
            .filter { if (type == null) true else it.type == type }
            .map { it.identifier }

        Timber.tag(TAG).d("Will refresh %s", toRefresh)

        toRefresh.forEach {
            modifyTest(it) { processor, test ->
                processor.markProcessing(test, true)
            }
        }

        val refreshedData = internalData.updateBlocking {
            val polling = values
                .filter { if (type == null) true else it.type == type }
                .filter { toRefresh.contains(it.identifier) }
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
            val pollingResults = polling.awaitAll()

            this.toMutableMap().apply {
                for (updatedResult in pollingResults) {
                    this[updatedResult.identifier] = updatedResult
                }
            }
        }

        toRefresh.forEach {
            modifyTest(it) { processor, test ->
                processor.markProcessing(test, false)
            }
        }

        return refreshedData.values.filter { toRefresh.contains(it.identifier) }.toSet()
    }

    suspend fun clear() {
        Timber.tag(TAG).i("clear()")
        internalData.updateBlocking {
            Timber.tag(TAG).d("Clearing %s", this)
            emptyMap()
        }
    }

    suspend fun markAsSubmitted(identifier: TestIdentifier) {
        Timber.tag(TAG).i("markAsSubmitted(identifier=%s)", identifier)

        modifyTest(identifier) { processor, before ->
            processor.markSubmitted(before)
        }
    }

    suspend fun markAsViewed(identifier: TestIdentifier) {
        Timber.tag(TAG).i("markAsViewed(identifier=%s)", identifier)

        modifyTest(identifier) { processor, before ->
            processor.markViewed(before)
        }
    }

    suspend fun updateConsent(identifier: TestIdentifier, consented: Boolean) {
        Timber.tag(TAG).i("updateConsent(identifier=%s, consented=%b)", identifier, consented)

        modifyTest(identifier) { processor, before ->
            processor.updateConsent(before, consented)
        }
    }

    suspend fun updateResultNotification(identifier: TestIdentifier, sent: Boolean) {
        Timber.tag(TAG).i("updateResultNotification(identifier=%s, sent=%b)", identifier, sent)

        modifyTest(identifier) { processor, before ->
            processor.updateResultNotification(before, sent)
        }
    }

    private suspend fun modifyTest(
        identifier: TestIdentifier,
        update: suspend (CoronaTestProcessor, CoronaTest) -> CoronaTest
    ) {
        internalData.updateBlocking {
            val original = values.singleOrNull { it.identifier == identifier }
                ?: throw CoronaTestNotFoundException("No test found for $identifier")

            val processor = getProcessor(original.type)

            val updated = update(processor, original)
            Timber.tag(TAG).d("Updated %s to %s", original, updated)

            toMutableMap().apply { this[original.identifier] = updated }
        }
    }

    companion object {
        const val TAG = "CoronaTestRepository"
    }
}
