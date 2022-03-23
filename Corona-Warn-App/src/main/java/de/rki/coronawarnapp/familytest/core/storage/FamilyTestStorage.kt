package de.rki.coronawarnapp.familytest.core.storage

import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FamilyTestStorage @Inject constructor(
) {

    val familyTestMap: Flow<Map<TestIdentifier, FamilyCoronaTest>> = flowOf(emptyMap())

    fun save(test: FamilyCoronaTest) {

    }

    fun update(test: FamilyCoronaTest) {

    }

    fun delete(test: FamilyCoronaTest) {

    }


}
