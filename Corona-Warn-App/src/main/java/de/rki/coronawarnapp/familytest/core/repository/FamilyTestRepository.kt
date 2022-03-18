package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.FamilyTest
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FamilyTestRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope
) {

    val familyTests: Flow<Set<FamilyTest>> = flowOf()
    val recycledFamilyTests: Flow<Set<FamilyTest>> = flowOf()

    suspend fun registerTest(
        request: TestRegistrationRequest,
        personName: String
    ): FamilyTest {
        // TBD
        throw NotImplementedError()
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
        // TBD
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
