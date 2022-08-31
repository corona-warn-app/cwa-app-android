package de.rki.coronawarnapp.familytest.core.repository

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.coronatest.errors.AlreadyRedeemedException
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.model.markAsNotified
import de.rki.coronawarnapp.familytest.core.model.markBadgeAsViewed
import de.rki.coronawarnapp.familytest.core.model.markDccCreated
import de.rki.coronawarnapp.familytest.core.model.markViewed
import de.rki.coronawarnapp.familytest.core.model.moveToRecycleBin
import de.rki.coronawarnapp.familytest.core.model.restore
import de.rki.coronawarnapp.familytest.core.model.updateLabId
import de.rki.coronawarnapp.familytest.core.model.updateResultNotification
import de.rki.coronawarnapp.familytest.core.model.updateSampleCollectedAt
import de.rki.coronawarnapp.familytest.core.model.updateTestResult
import de.rki.coronawarnapp.familytest.core.notification.FamilyTestNotificationService
import de.rki.coronawarnapp.familytest.core.repository.CoronaTestProcessor.ServerResponse.CoronaTestResultUpdate
import de.rki.coronawarnapp.familytest.core.repository.CoronaTestProcessor.ServerResponse.Error
import de.rki.coronawarnapp.familytest.core.storage.FamilyTestStorage
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyTestRepository @Inject constructor(
    private val processor: CoronaTestProcessor,
    private val storage: FamilyTestStorage,
    private val timeStamper: TimeStamper,
    private val familyTestNotificationService: FamilyTestNotificationService
) {

    val familyTests: Flow<Set<FamilyCoronaTest>> = storage.familyTestMap.map {
        it.values.toSet()
    }

    val familyTestsInRecycleBin: Flow<Set<FamilyCoronaTest>> = storage.familyTestRecycleBinMap.map {
        it.values.toSet()
    }

    val familyTestsToRefresh: Flow<Set<FamilyCoronaTest>> = familyTests.map {
        it.filterNot {
            it.coronaTest.isPollingStopped()
        }.toSet()
    }

    suspend fun registerTest(
        qrCode: CoronaTestQRCode,
        personName: String
    ): FamilyCoronaTest {
        val coronaTest = processor.register(qrCode)
        if (coronaTest.isRedeemed) throw AlreadyRedeemedException(coronaTest)
        return FamilyCoronaTest(
            personName = personName,
            coronaTest = coronaTest
        ).also { test ->
            storage.save(test)
        }
    }

    suspend fun refresh(): Map<TestIdentifier, Exception> {
        val exceptions = mutableMapOf<TestIdentifier, Exception>()
        val updates = mutableListOf<Pair<TestIdentifier, (FamilyCoronaTest) -> FamilyCoronaTest>>()
        familyTestsToRefresh.first().forEach { originalTest ->
            when (val updateResult = processor.pollServer(originalTest.coronaTest)) {
                is CoronaTestResultUpdate ->
                    updates.add(
                        Pair(
                            originalTest.identifier
                        ) { test ->
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
                    )
                is Error -> exceptions[originalTest.identifier] = updateResult.error
            }
        }

        if (updates.isNotEmpty()) {
            storage.update(updates)
        }

        notifyIfNeeded()

        return exceptions
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun notifyIfNeeded() {
        val familyTestResultChanges = familyTests.first().filter {
            it.hasResultChangeBadge && !it.isResultAvailableNotificationSent
        }

        if (familyTestResultChanges.isNotEmpty()) {
            Timber.tag(TAG).d("Notifying about [%s] family test results", familyTestResultChanges.size)
            familyTestNotificationService.showTestResultNotification()
        } else {
            Timber.tag(TAG).d("No notification required for family tests")
        }

        val updates = mutableListOf<Pair<TestIdentifier, (FamilyCoronaTest) -> FamilyCoronaTest>>()
        familyTestResultChanges.forEach {
            Timber.tag(TAG).d("Mark test=%s as notified", it.identifier)
            updates.add(
                Pair(it.identifier) { test ->
                    test.copy(coronaTest = test.coronaTest.markAsNotified(true))
                }
            )
        }
        if (updates.isNotEmpty()) {
            storage.update(updates)
        }
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

    suspend fun moveAllTestsToRecycleBin(identifiers: List<TestIdentifier>) {
        storage.moveAllToRecycleBin(identifiers, timeStamper.nowUTC)
    }

    suspend fun deleteTest(
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

    suspend fun markAllBadgesAsViewed(
        identifiers: List<TestIdentifier>
    ) {
        identifiers.map {
            Pair<TestIdentifier, (FamilyCoronaTest) -> FamilyCoronaTest>(it) { test ->
                test.markBadgeAsViewed()
            }
        }.let {
            storage.update(it)
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

    suspend fun markAsNotified(identifier: TestIdentifier, notified: Boolean) {
        storage.update(identifier) { test ->
            test.copy(coronaTest = test.coronaTest.markAsNotified(notified))
        }
    }

    private suspend fun getTest(identifier: TestIdentifier) =
        storage.familyTestMap.first()[identifier] ?: storage.familyTestRecycleBinMap.first()[identifier]

    companion object {
        private val TAG = tag<FamilyTestRepository>()
    }
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
