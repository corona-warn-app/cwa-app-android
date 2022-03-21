package de.rki.coronawarnapp.qrcode.handler

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.coronatest.request.toRestoreRecycledTestRequest
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class CoronaTestQRCodeHandler @Inject constructor(
    private val recycledCoronaTestsProvider: RecycledCoronaTestsProvider,
    private val submissionRepository: SubmissionRepository
) {
    suspend fun handleQrCode(qrCode: CoronaTestQRCode): CoronaTestResult {
        Timber.tag(TAG).d("handleQrCode(qrCode=%s)", qrCode::class.java.simpleName)
        val recycledCoronaTest = recycledCoronaTestsProvider.findCoronaTest(qrCode.rawQrCode.toSHA256())
        return when {
            recycledCoronaTest != null -> CoronaTestResult.InRecycleBin(recycledCoronaTest)
            else -> CoronaTestResult.TestRegistrationSelection(qrCode)
        }.also { Timber.tag(TAG).d("returning %s", it::class.simpleName) }
    }

    suspend fun restoreCoronaTest(recycledCoronaTest: CoronaTest): CoronaTestResult {
        Timber.tag(TAG).d("restoreCoronaTest(recycledCoronaTest=%s)", recycledCoronaTest::class.java.simpleName)
        val currentCoronaTest = submissionRepository.testForType(recycledCoronaTest.type).first()
        return when {
            currentCoronaTest != null -> CoronaTestResult.RestoreDuplicateTest(
                recycledCoronaTest.toRestoreRecycledTestRequest()
            )

            else -> {
                recycledCoronaTestsProvider.restoreCoronaTest(recycledCoronaTest.identifier)
                recycledCoronaTest.toCoronaTestResult()
            }
        }.also { Timber.tag(TAG).d("returning %s", it::class.java.simpleName) }
    }

    private fun CoronaTest.toCoronaTestResult(): CoronaTestResult = when {
        isPending -> CoronaTestResult.TestPending(test = this)
        isNegative -> CoronaTestResult.TestNegative(test = this)
        isPositive -> when (isAdvancedConsentGiven) {
            true -> CoronaTestResult.TestPositive(test = this)
            false -> CoronaTestResult.WarnOthers(test = this)
        }
        else -> CoronaTestResult.TestInvalid(test = this)
    }

    companion object {
        private val TAG = tag<CoronaTestQRCodeHandler>()
    }
}
