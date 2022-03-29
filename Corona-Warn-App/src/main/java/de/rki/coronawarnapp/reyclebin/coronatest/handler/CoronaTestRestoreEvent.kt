package de.rki.coronawarnapp.reyclebin.coronatest.handler

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.request.RestoreRecycledTestRequest

sealed interface CoronaTestRestoreEvent {
    data class RestoredTest(val restoredTest: BaseCoronaTest) : CoronaTestRestoreEvent
    data class RestoreDuplicateTest(val restoreRecycledTestRequest: RestoreRecycledTestRequest) : CoronaTestRestoreEvent
}
