package de.rki.coronawarnapp.coronatest.type.rapidantigen

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.TimeStamper
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RapidAntigenProcessor @Inject constructor(
    private val timeStamper: TimeStamper,
    private val submissionService: CoronaTestService,
) : CoronaTestProcessor {

    override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN

    override suspend fun create(request: CoronaTestQRCode): RapidAntigenCoronaTest {
        Timber.tag(TAG).d("create(data=%s)", request)
        request as CoronaTestQRCode.RapidAntigen

        val registrationData = submissionService.asyncRegisterDeviceViaGUID(request.qrCodeGUID)

        registrationData.testResult.validOrThrow()

        return RapidAntigenCoronaTest(
            identifier = request.identifier,
            registeredAt = timeStamper.nowUTC,
            registrationToken = registrationData.registrationToken,
            testResult = registrationData.testResult,
            testedAt = request.createdAt,
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
        )
    }

    override suspend fun create(request: CoronaTestTAN): CoronaTest {
        Timber.tag(TAG).d("create(data=%s)", request)
        request as CoronaTestTAN.RapidAntigen
        throw UnsupportedOperationException("There are no TAN based RATs")
    }

    override suspend fun pollServer(test: CoronaTest): CoronaTest {
        return try {
            Timber.tag(TAG).v("pollServer(test=%s)", test)
            test as RapidAntigenCoronaTest

            if (test.isSubmitted || test.isSubmissionAllowed) {
                Timber.tag(TAG).w("Not refreshing already final test.")
                return test
            }

            val testResult = submissionService.asyncRequestTestResult(test.registrationToken)
            Timber.tag(TAG).d("Test result was %s", testResult)

            test.copy(
                testResult = testResult,
                lastError = null
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to poll server for  %s", test)
            if (e !is CwaWebException) e.report(ExceptionCategory.INTERNAL)

            test as RapidAntigenCoronaTest
            test.copy(lastError = e)
        }
    }

    override suspend fun onRemove(toBeRemoved: CoronaTest) {
        Timber.tag(TAG).v("onRemove(toBeRemoved=%s)", toBeRemoved)
        // Currently nothing to do
    }

    override suspend fun markSubmitted(test: CoronaTest): RapidAntigenCoronaTest {
        Timber.tag(TAG).d("markSubmitted(test=%s)", test)
        test as RapidAntigenCoronaTest

        return test.copy(isSubmitted = true)
    }

    override suspend fun markProcessing(test: CoronaTest, isProcessing: Boolean): CoronaTest {
        Timber.tag(TAG).v("markProcessing(test=%s, isProcessing=%b)", test, isProcessing)
        test as RapidAntigenCoronaTest

        return test.copy(isProcessing = isProcessing)
    }

    override suspend fun markViewed(test: CoronaTest): CoronaTest {
        Timber.tag(TAG).v("markViewed(test=%s)", test)
        test as RapidAntigenCoronaTest

        return test.copy(isViewed = true)
    }

    override suspend fun updateConsent(test: CoronaTest, consented: Boolean): CoronaTest {
        Timber.tag(TAG).v("updateConsent(test=%s, consented=%b)", test, consented)
        test as RapidAntigenCoronaTest

        return test.copy(isAdvancedConsentGiven = consented)
    }

    override suspend fun updateResultNotification(test: CoronaTest, sent: Boolean): CoronaTest {
        Timber.tag(TAG).v("updateResultNotification(test=%s, sent=%b)", test, sent)
        test as RapidAntigenCoronaTest

        return test.copy(isResultAvailableNotificationSent = sent)
    }

    companion object {
        private const val TAG = "RapidAntigenProcessor"
    }
}

private fun CoronaTestResult.validOrThrow() {
    val isValid = when (this) {
        CoronaTestResult.PCR_OR_RAT_PENDING,
        CoronaTestResult.RAT_PENDING,
        CoronaTestResult.RAT_NEGATIVE,
        CoronaTestResult.RAT_POSITIVE,
        CoronaTestResult.RAT_INVALID,
        CoronaTestResult.RAT_REDEEMED -> true

        CoronaTestResult.PCR_NEGATIVE,
        CoronaTestResult.PCR_POSITIVE,
        CoronaTestResult.PCR_INVALID,
        CoronaTestResult.PCR_REDEEMED -> false
    }

    if (!isValid) throw IllegalArgumentException("Invalid testResult $this")
}
