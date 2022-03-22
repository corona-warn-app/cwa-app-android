package de.rki.coronawarnapp.reyclebin.coronatest.handler

import de.rki.coronawarnapp.reyclebin.coronatest.request.RestoreRecycledTestRequest

sealed interface CoronaTestRestoreEvent {
    object NotYetImplemented: CoronaTestRestoreEvent
    data class RestoreDuplicateTest(val restoreRecycledTestRequest: RestoreRecycledTestRequest): CoronaTestRestoreEvent
}
