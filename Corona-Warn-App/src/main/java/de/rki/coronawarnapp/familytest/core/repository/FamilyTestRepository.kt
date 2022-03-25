package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.CoronaTest.State
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.model.markBadgeAsViewed
import de.rki.coronawarnapp.familytest.core.model.markDccCreated
import de.rki.coronawarnapp.familytest.core.model.markViewed
import de.rki.coronawarnapp.familytest.core.model.recycle
import de.rki.coronawarnapp.familytest.core.model.restore
import de.rki.coronawarnapp.familytest.core.model.updateResultNotification
import de.rki.coronawarnapp.familytest.core.storage.FamilyTestStorage
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlinx.coroutines.plus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyTestRepository @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    @AppScope private val appScope: CoroutineScope,
    private val processor: BaseCoronaTestProcessor,
    private val storage: FamilyTestStorage,
    private val timeStamper: TimeStamper,
) {

    private val familyTestMap = storage.familyTestMap

    val familyTests: Flow<Set<FamilyCoronaTest>> = storage.familyTestMap.map {
        it.values.filter { test -> test.isNotRecycled }.toSet()
    }.shareLatest(
        tag = TAG,
        scope = appScope + dispatcherProvider.IO
    )

    val familyTestRecycleBin: Flow<Set<FamilyCoronaTest>> = storage.familyTestMap.map {
        it.values.filter { test -> test.isRecycled }.toSet()
    }.shareLatest(
        tag = TAG,
        scope = appScope + dispatcherProvider.IO
    )

    suspend fun registerTest(
        qrCode: CoronaTestQRCode,
        personName: String
    ): FamilyCoronaTest {
        val test = FamilyCoronaTest(
            personName = personName,
            coronaTest = processor.register(qrCode)
        )
        storage.save(test)
        return test
    }

    suspend fun restoreTest(
        identifier: TestIdentifier
    ) {
        val test = getTest(identifier) ?: return
        val updated = test.copy(coronaTest = test.coronaTest.restore())
        storage.update(updated)
    }

    suspend fun moveTestToRecycleBin(
        identifier: TestIdentifier
    ) {
        val test = getTest(identifier) ?: return
        val updated = test.copy(coronaTest = test.coronaTest.recycle(timeStamper.nowUTC))
        storage.update(updated)
    }

    suspend fun deleteTest(
        identifier: TestIdentifier
    ) {
        val test = getTest(identifier) ?: return
        storage.delete(test)
    }

    suspend fun refresh(forceRefresh: Boolean = false) {
        val refreshed = familyTests.first().map { familyTest ->
            val oldState = familyTest.coronaTest.state
            val updatedTest = processor.pollServer(familyTest.coronaTest, forceRefresh)
            val newState = updatedTest.state

            FamilyCoronaTest(
                familyTest.personName,
                updatedTest.copy(
                    uiState = updatedTest.uiState.copy(
                        hasResultChangeBadge = testHasInterestingResultChange(oldState, newState)
                    )
                )
            )
        }

        refreshed.forEach {
            storage.update(it)
        }
    }

    suspend fun markViewed(
        identifier: TestIdentifier
    ) {
        val test = getTest(identifier) ?: return
        val updated = test.copy(coronaTest = test.coronaTest.markViewed())
        storage.update(updated)
    }

    suspend fun markBadgeAsViewed(
        identifier: TestIdentifier
    ) {
        val test = getTest(identifier) ?: return
        val updated = test.copy(coronaTest = test.coronaTest.markBadgeAsViewed())
        storage.update(updated)
    }

    suspend fun updateResultNotification(
        identifier: TestIdentifier,
        sent: Boolean
    ) {
        val test = getTest(identifier) ?: return
        val updated = test.copy(coronaTest = test.coronaTest.updateResultNotification(sent))
        storage.update(updated)
    }

    suspend fun markDccAsCreated(
        identifier: TestIdentifier,
        created: Boolean
    ) {
        val test = getTest(identifier) ?: return
        val updated = test.copy(coronaTest = test.coronaTest.markDccCreated(created))
        storage.update(updated)
    }

    suspend fun clear() {
        // TBD
    }

    private suspend fun getTest(identifier: TestIdentifier) = familyTestMap.first()[identifier]

    companion object {
        private val TAG = tag<FamilyTestRepository>()
    }
}

fun testHasInterestingResultChange(
    oldState: State,
    newState: State
): Boolean {
    Timber.tag("FamilyTestRepository").d("oldState=%s newState=%s", oldState, newState)
    val states = setOf(
        State.POSITIVE,
        State.NEGATIVE,
        State.INVALID
    )

    return when {
        // Recycled is not an actual test state
        // ex: Positive -> Recycled -> Positive -> false change
        oldState == State.RECYCLED || newState == State.RECYCLED -> false
        else -> oldState != newState && newState in states
    }
}
