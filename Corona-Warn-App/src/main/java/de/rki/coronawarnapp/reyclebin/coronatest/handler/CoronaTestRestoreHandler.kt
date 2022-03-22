package de.rki.coronawarnapp.reyclebin.coronatest.handler

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.coronatest.request.toRestoreRecycledTestRequest
import de.rki.coronawarnapp.submission.SubmissionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CoronaTestRestoreHandler @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val recycledCoronaTestsProvider: RecycledCoronaTestsProvider
){

    suspend fun restoreCoronaTest(recycledCoronaTest: CoronaTest): CoronaTestRestoreEvent {
        /* to do */
        return CoronaTestRestoreEvent.NotYetImplemented
    }

    private suspend fun restorePersonalCoronaTest(recycledCoronaTest: PersonalCoronaTest) {
        val currentCoronaTest = submissionRepository.testForType(recycledCoronaTest.type).first()
        when {
            currentCoronaTest != null -> CoronaTestResult.RestoreDuplicateTest(
                recycledCoronaTest.toRestoreRecycledTestRequest()
            )

            else -> {
                recycledCoronaTestsProvider.restoreCoronaTest(recycledCoronaTest.identifier)
                recycledCoronaTest.toCoronaTestResult()
            }
        }
    }

    private fun PersonalCoronaTest.toCoronaTestResult(): CoronaTestResult = when {
        isPending -> CoronaTestResult.TestPending(test = this)
        isNegative -> CoronaTestResult.TestNegative(test = this)
        isPositive -> when (isAdvancedConsentGiven) {
            true -> CoronaTestResult.TestPositive(test = this)
            false -> CoronaTestResult.WarnOthers(test = this)
        }
        else -> CoronaTestResult.TestInvalid(test = this)
    }
}
