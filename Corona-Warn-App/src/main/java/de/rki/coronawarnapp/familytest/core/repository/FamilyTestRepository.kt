package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FamilyTestRepository @Inject constructor(
    private val processor: BaseCoronaTestProcessor
) {

    private val familyTestMap: Flow<Map<TestIdentifier, FamilyCoronaTest>> = flowOf(emptyMap())

    val familyTests: Flow<Set<FamilyCoronaTest>> = familyTestMap.map { it.values.toSet() }
    val recycledFamilyTests: Flow<Set<FamilyCoronaTest>> = flowOf(emptySet())


    suspend fun registerTest(
        qrCode: CoronaTestQRCode,
        personName: String
    ): FamilyCoronaTest {
        val test = FamilyCoronaTest(
            personName = personName,
            coronaTest = processor.register(qrCode)
        )

        // store test

        return test
    }

    suspend fun restoreTest(
        identifier: TestIdentifier
    ) {
        // TBD
    }

    suspend fun recycleTest(
        identifier: TestIdentifier
    ) {
        // TBD
    }

    suspend fun deleteTest(
        identifier: TestIdentifier
    ) {
        // TBD
    }

    /**
     * Try to refresh available family tests
     * Does not throw any error
     */
    suspend fun refresh() {
        val refreshed = familyTests.first().map {
            val updatedTest = processor.pollServer(it.coronaTest)
            FamilyCoronaTest(
                it.personName,
                updatedTest
            )
        }

        // store refreshed
    }

    suspend fun markBadgeAsViewed(
        identifier: TestIdentifier
    ) {
        // TBD
    }

    suspend fun updateResultNotification(
        identifier: TestIdentifier,
        sent: Boolean
    ) {
        // TBD
    }

    suspend fun markDccAsCreated(
        identifier: TestIdentifier,
        created: Boolean
    ) {
        // TBD
    }

    suspend fun clear() {
        // TBD
    }
}
