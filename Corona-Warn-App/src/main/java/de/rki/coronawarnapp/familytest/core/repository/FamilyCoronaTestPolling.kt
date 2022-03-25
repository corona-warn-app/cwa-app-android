package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.familytest.core.model.CoronaTest

fun CoronaTest.isPollingStopped(forceUpdate: Boolean): Boolean = (!forceUpdate && testResult in finalStates)

private val finalStates = setOf(
    CoronaTestResult.PCR_POSITIVE,
    CoronaTestResult.PCR_NEGATIVE,
    CoronaTestResult.PCR_OR_RAT_REDEEMED,
    CoronaTestResult.RAT_REDEEMED,
    CoronaTestResult.RAT_POSITIVE,
    CoronaTestResult.RAT_NEGATIVE,
)
