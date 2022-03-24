package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.coronatest.type.isOlderThan21Days
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.model.markBadgeAsViewed
import de.rki.coronawarnapp.familytest.core.model.markDccCreated
import de.rki.coronawarnapp.familytest.core.model.markViewed
import de.rki.coronawarnapp.familytest.core.model.recycle
import de.rki.coronawarnapp.familytest.core.model.restore
import de.rki.coronawarnapp.familytest.core.model.updateLabId
import de.rki.coronawarnapp.familytest.core.model.updateResultNotification
import de.rki.coronawarnapp.familytest.core.model.updateTestResult
import de.rki.coronawarnapp.familytest.core.storage.FamilyTestStorage
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyTestRepository @Inject constructor(
    private val processor: BaseCoronaTestProcessor,
    private val storage: FamilyTestStorage,
    private val timeStamper: TimeStamper,
) {

    val familyTests: Flow<Set<FamilyCoronaTest>> = storage.familyTestMap.map {
        it.values.toSet()
    }

    val familyTestRecycleBin: Flow<Set<FamilyCoronaTest>> = storage.familyTestRecycleBinMap.map {
        it.values.filter { it.isRecycled }.toSet()
    }

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
        storage.update(identifier) { test ->
            test.copy(coronaTest = test.coronaTest.restore())
        }
    }

    suspend fun moveTestToRecycleBin(
        identifier: TestIdentifier
    ) {
        storage.update(identifier) { test ->
            test.copy(coronaTest = test.coronaTest.recycle(timeStamper.nowUTC))
        }
    }

    suspend fun deleteTest(
        identifier: TestIdentifier
    ) {
        val test = getTest(identifier) ?: return
        storage.delete(test)
    }

    suspend fun refresh(forceRefresh: Boolean = false) {
        familyTests.first().map {
            if (it.coronaTest.isPollingStopped(forceRefresh, timeStamper.nowUTC)) null
            else {
                val updateResult = processor.pollServer(it.coronaTest, forceRefresh) ?: return@map null
                storage.update(it.identifier) { test ->
                    val coronaTest = test.coronaTest.updateTestResult(updateResult.coronaTestResult).let { updated ->
                        updateResult.labId?.let { labId -> updated.updateLabId(labId) } ?: updated
                    }
                    test.copy(
                        coronaTest = coronaTest)
                }
            }
        }
    }

    suspend fun markViewed(
        identifier: TestIdentifier
    ) {
        storage.update(identifier) { test ->
            test.copy(coronaTest = test.coronaTest.markViewed())
        }
    }

    suspend fun markBadgeAsViewed(
        identifier: TestIdentifier
    ) {
        storage.update(identifier) { test ->
            test.copy(coronaTest = test.coronaTest.markBadgeAsViewed())
        }
    }

    suspend fun updateResultNotification(
        identifier: TestIdentifier,
        sent: Boolean
    ) {
        storage.update(identifier) { test ->
            test.copy(coronaTest = test.coronaTest.updateResultNotification(sent))
        }
    }

    suspend fun markDccAsCreated(
        identifier: TestIdentifier,
        created: Boolean
    ) {
        storage.update(identifier) { test ->
            test.copy(coronaTest = test.coronaTest.markDccCreated(created))
        }
    }

    suspend fun clear() {
        storage.clear()
    }

    private suspend fun getTest(identifier: TestIdentifier) = storage.familyTestMap.first()[identifier]
}

private fun CoronaTest.isPollingStopped(forceUpdate: Boolean, now: Instant): Boolean =
    (!forceUpdate && testResult in finalStates) || isOlderThan21Days(now) && testResult in redeemedStates

private val finalStates = setOf(
    CoronaTestResult.PCR_POSITIVE,
    CoronaTestResult.PCR_NEGATIVE,
    CoronaTestResult.PCR_OR_RAT_REDEEMED,
    CoronaTestResult.RAT_REDEEMED,
    CoronaTestResult.RAT_POSITIVE,
    CoronaTestResult.RAT_NEGATIVE,
    CoronaTestResult.PCR_INVALID,
    CoronaTestResult.RAT_INVALID
)

private val redeemedStates = setOf(CoronaTestResult.PCR_OR_RAT_REDEEMED, CoronaTestResult.RAT_REDEEMED)

