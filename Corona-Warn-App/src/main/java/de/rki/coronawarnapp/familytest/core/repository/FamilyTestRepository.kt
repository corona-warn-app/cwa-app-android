package de.rki.coronawarnapp.familytest.core.repository

import androidx.annotation.VisibleForTesting
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
import de.rki.coronawarnapp.familytest.core.model.updateFromResponse
import de.rki.coronawarnapp.familytest.core.notification.FamilyTestNotificationService
import de.rki.coronawarnapp.familytest.core.repository.CoronaTestProcessor.ServerResponse.Success
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
        familyTests.first().filterNot {
            it.coronaTest.isPollingStopped()
        }.forEach { originalTest ->
            when (val updateResult = processor.pollServer(originalTest.coronaTest)) {
                is Success -> storage.update(originalTest.identifier) { test ->
                    test.updateFromResponse(updateResult)
                }
                is Error -> exceptions[originalTest.identifier] = updateResult.error
            }
        }

        notifyIfNeeded()

        return exceptions
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun notifyIfNeeded() {
        val identifiers = familyTests.first()
            .filter { it.hasResultChangeBadge && !it.isResultAvailableNotificationSent }
            .map { it.identifier }
            .toSet()

        if (identifiers.isNotEmpty()) {
            Timber.tag(TAG).d("Notifying about [%s] family test results", identifiers.size)
            familyTestNotificationService.showTestResultNotification()
            storage.updateAll(identifiers) { test ->
                Timber.tag(TAG).d("Mark test=%s as notified", test.identifier)
                test.markAsNotified(true)
            }
        } else {
            Timber.tag(TAG).d("No notification required for family tests")
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

    suspend fun markBadgeAsViewed(
        identifier: TestIdentifier
    ) {
        storage.update(identifier) { test ->
            test.markBadgeAsViewed()
        }
    }

    suspend fun markAllBadgesAsViewed() {
        val identifiers = familyTests.first().filter { it.hasBadge }.map { it.identifier }.toSet()
        storage.updateAll(identifiers) { test ->
            test.markBadgeAsViewed()
        }
    }

    suspend fun moveAllToRecycleBin() {
        val identifiers = familyTests.first().map { it.identifier }.toSet()
        storage.updateAll(identifiers) { test ->
            test.moveToRecycleBin(timeStamper.nowUTC)
        }
    }

    suspend fun updateResultNotification(
        identifier: TestIdentifier,
        sent: Boolean
    ) {
        storage.update(identifier) { test ->
            test.markAsNotified(sent)
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
