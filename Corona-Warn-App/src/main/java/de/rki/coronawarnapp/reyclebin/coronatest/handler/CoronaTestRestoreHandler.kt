package de.rki.coronawarnapp.reyclebin.coronatest.handler

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import javax.inject.Inject

class CoronaTestRestoreHandler @Inject constructor(){

    suspend fun restoreCoronaTest(recycledCoronaTest: CoronaTest): CoronaTestRestoreEvent {
        /* to do */
        return CoronaTestRestoreEvent.NotYetImplemented
    }
}
