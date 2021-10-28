package de.rki.coronawarnapp.reyclebin.coronatest

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecycledCoronaTestsRepository @Inject constructor(
    private val coronaTestRepository: CoronaTestRepository,
    private val recycledCoronaTestsStorage: RecycledCoronaTestsStorage,

) {

    val tests: Flow<Set<CoronaTest>> = recycledCoronaTestsStorage.tests

    suspend fun findCoronaTest(coronaTestQrCodeHash: String): RecycledCoronaTest? {
        return recycledCoronaTestsStorage.findTest(coronaTestQrCodeHash)
    }

    suspend fun addCoronTest(coronaTest: CoronaTest) {
        // TODO
    }

    suspend fun restoreCoronaTest(coronaTestIdentifier: TestIdentifier) {
        // TODO
    }

    suspend fun deleteCoronaTest(recycledCoronaTest: CoronaTest) {
        // TODO
    }

    suspend fun deleteAllCoronaTest(recycledCoronaTests: Set<CoronaTest>) {
        // TODO
    }

    suspend fun clear() {
        // TODO
    }
}
