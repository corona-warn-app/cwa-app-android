package de.rki.coronawarnapp.coronatest.type.pcr

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.util.TimeStamper
import timber.log.Timber
import javax.inject.Inject

@Reusable
class PCRProcessor @Inject constructor(
    private val timeStamper: TimeStamper,
    private val verificationServer: VerificationServer,
) : CoronaTestProcessor {

    override val type: CoronaTest.Type = CoronaTest.Type.PCR

    override suspend fun create(data: CoronaTestQRCode): PCRCoronaTest {
        Timber.tag(TAG).d("create(data=%s)", data)
        data as CoronaTestQRCode.PCR

        val registrationToken: RegistrationToken = verificationServer.retrieveRegistrationToken(
            key = data.guid,
            keyType = VerificationKeyType.GUID
        )

        return PCRCoronaTest(
            testGUID = data.guid,
            registeredAt = timeStamper.nowUTC,
            registrationToken = registrationToken,
            testResult = CoronaTestResult.PCR_OR_RAT_PENDING,
        )
    }

    override suspend fun pollServer(test: CoronaTest): CoronaTest = try {
        Timber.tag(TAG).v("pollServer(test=%s)", test)
        test as PCRCoronaTest

        val testResult = verificationServer.pollTestResult(test.registrationToken)
        Timber.tag(TAG).d("Test result was %s", testResult)

        val isValid = when (testResult) {
            CoronaTestResult.PCR_OR_RAT_PENDING,
            CoronaTestResult.PCR_NEGATIVE,
            CoronaTestResult.PCR_POSITIVE,
            CoronaTestResult.PCR_INVALID,
            CoronaTestResult.PCR_REDEEMED -> true

            CoronaTestResult.RAT_PENDING,
            CoronaTestResult.RAT_NEGATIVE,
            CoronaTestResult.RAT_POSITIVE,
            CoronaTestResult.RAT_INVALID,
            CoronaTestResult.RAT_REDEEMED -> false
        }

        if (!isValid) throw IllegalArgumentException("Invalid testResult $testResult")

        test.copy(
            testResult = testResult,
            lastError = null
        )
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to poll server for  %s", test)
        test as PCRCoronaTest
        test.copy(lastError = e)
    }

    override suspend fun onRemove(toBeRemoved: CoronaTest) {
        Timber.tag(TAG).v("onRemove(toBeRemoved=%s)", toBeRemoved)
        // Currently nothing to do
    }

    override suspend fun markSubmitted(test: CoronaTest): PCRCoronaTest {
        Timber.tag(TAG).v("markSubmitted(test=%s)", test)
        test as PCRCoronaTest

        return test.copy(isSubmitted = true)
    }

    override suspend fun markProcessing(test: CoronaTest, isProcessing: Boolean): CoronaTest {
        Timber.tag(TAG).v("markProcessing(test=%s, isProcessing=%b)", test, isProcessing)
        test as PCRCoronaTest

        return test.copy(isProcessing = true)
    }

    companion object {
        private const val TAG = "PCRProcessor"
    }
}
