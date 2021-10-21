package de.rki.coronawarnapp.reyclebin.coronatest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class RecycledCoronaTestsStorage @Inject constructor() {

    // TODO
    val tests: Flow<Set<RecycledCoronaTest>> = emptyFlow()

    suspend fun findTest(coronaTestQrCodeHash: String): RecycledCoronaTest? {
        // TODO
        @Suppress("FunctionOnlyReturningConstant")
        return null
    }
}
