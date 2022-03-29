package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.model.markBadgeAsViewed
import de.rki.coronawarnapp.familytest.core.model.markDccCreated
import de.rki.coronawarnapp.familytest.core.model.markViewed
import de.rki.coronawarnapp.familytest.core.model.moveToRecycleBin
import de.rki.coronawarnapp.familytest.core.model.restore
import de.rki.coronawarnapp.familytest.core.model.updateLabId
import de.rki.coronawarnapp.familytest.core.model.updateResultNotification
import de.rki.coronawarnapp.familytest.core.model.updateSampleCollectedAt
import de.rki.coronawarnapp.familytest.core.model.updateTestResult
import de.rki.coronawarnapp.familytest.core.repository.CoronaTestProcessor.ServerResponse.CoronaTestResultUpdate
import de.rki.coronawarnapp.familytest.core.repository.CoronaTestProcessor.ServerResponse.Error
import de.rki.coronawarnapp.familytest.core.storage.FamilyTestStorage
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyTestRepository @Inject constructor(
    private val processor: CoronaTestProcessor,
    private val storage: FamilyTestStorage,
    private val timeStamper: TimeStamper,
) {

    val familyTests: Flow<Set<FamilyCoronaTest>> = storage.familyTestMap.map {
        it.values.toSet()
    }

    val familyTestRecycleBin: Flow<Set<FamilyCoronaTest>> = storage.familyTestRecycleBinMap.map {
        it.values.toSet()
    }

    suspend fun registerTest(
        qrCode: CoronaTestQRCode,
        personName: String
    ): FamilyCoronaTest {
        return FamilyCoronaTest(
            personName = personName,
            coronaTest = processor.register(qrCode)
        ).also { test ->
            storage.save(test)
        }
    }

    suspend fun refresh(): Map<TestIdentifier, Exception> {
        val exceptions = mutableMapOf<TestIdentifier, Exception>()
        familyTests.first().filter {
            !it.coronaTest.isPollingStopped()
        }.forEach { originalTest ->
            when (val updateResult = processor.pollServer(originalTest.coronaTest)) {
                is CoronaTestResultUpdate ->
                    storage.update(originalTest.identifier) { test ->
                        test.updateTestResult(
                            updateResult.coronaTestResult
                        ).let { updated ->
                            updateResult.labId?.let { labId ->
                                updated.updateLabId(labId)
                            } ?: updated
                        }.let { updated ->
                            updateResult.sampleCollectedAt?.let { collectedAt ->
                                updated.updateSampleCollectedAt(collectedAt)
                            } ?: updated
                        }
                    }
                is Error -> exceptions[originalTest.identifier] = updateResult.error
            }
        }

        return exceptions
    }

    suspend fun restoreTest(
        identifier: TestIdentifier
    ) {
        storage.update(identifier) { test ->
            test.restore()
        }
    }

    suspend fun moveTestToRecycleBin(
        identifier: TestIdentifier
    ) {
        storage.update(identifier) { test ->
            test.moveToRecycleBin(timeStamper.nowUTC)
        }
    }

    suspend fun removeTest(
        identifier: TestIdentifier
    ) {
        val test = getTest(identifier) ?: return
        storage.delete(test)
    }

    suspend fun markViewed(
        identifier: TestIdentifier
    ) {
        storage.update(identifier) { test ->
            test.markViewed()
        }
    }

    suspend fun markBadgeAsViewed(
        identifier: TestIdentifier
    ) {
        storage.update(identifier) { test ->
            test.markBadgeAsViewed()
        }
    }

    suspend fun updateResultNotification(
        identifier: TestIdentifier,
        sent: Boolean
    ) {
        storage.update(identifier) { test ->
            test.updateResultNotification(sent)
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

    private suspend fun getTest(identifier: TestIdentifier) =
        storage.familyTestMap.first()[identifier] ?: storage.familyTestRecycleBinMap.first()[identifier]
}

private fun CoronaTest.isPollingStopped(): Boolean = testResult in finalStates

private val finalStates = setOf(
    CoronaTestResult.PCR_POSITIVE,
    CoronaTestResult.PCR_NEGATIVE,
    CoronaTestResult.PCR_OR_RAT_REDEEMED,
    CoronaTestResult.RAT_REDEEMED,
    CoronaTestResult.RAT_POSITIVE,
    CoronaTestResult.RAT_NEGATIVE,
)
