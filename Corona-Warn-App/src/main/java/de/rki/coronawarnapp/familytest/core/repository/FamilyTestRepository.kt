package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.model.markBadgeAsViewed
import de.rki.coronawarnapp.familytest.core.model.markDccCreated
import de.rki.coronawarnapp.familytest.core.model.markAsViewed
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

    val recycledTests: Flow<Set<FamilyCoronaTest>> = storage.familyTestMap.map {
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

    suspend fun removeTest(
        identifier: TestIdentifier
    ) {
        val test = getTest(identifier) ?: return
        storage.delete(test)
    }

    suspend fun refresh(forceRefresh: Boolean = false) {
        val refreshed = familyTests.first().map {
            val updatedTest = processor.pollServer(it.coronaTest, forceRefresh)
            FamilyCoronaTest(
                it.personName,
                updatedTest
            )
        }

        refreshed.forEach {
            storage.update(it)
        }
    }

    suspend fun markAsViewed(
        identifier: TestIdentifier
    ) {
        val test = getTest(identifier) ?: return
        val updated = test.copy(coronaTest = test.coronaTest.markAsViewed())
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
