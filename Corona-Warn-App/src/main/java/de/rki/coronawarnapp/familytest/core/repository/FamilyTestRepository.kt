package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.FamilyTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FamilyTestRepository @Inject constructor() {

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
