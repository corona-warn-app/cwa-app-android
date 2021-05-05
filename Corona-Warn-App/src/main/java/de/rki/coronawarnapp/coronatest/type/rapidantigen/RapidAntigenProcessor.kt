package de.rki.coronawarnapp.coronatest.type.rapidantigen

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_REDEEMED
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.coronatest.type.isOlderThan21Days
import de.rki.coronawarnapp.coronatest.type.pcr.PCRProcessor
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RapidAntigenProcessor @Inject constructor(
    private val timeStamper: TimeStamper,
    private val submissionService: CoronaTestService,
) : CoronaTestProcessor {

    override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN

    override suspend fun create(request: CoronaTestQRCode): RACoronaTest {
        Timber.tag(TAG).d("create(data=%s)", request)
        request as CoronaTestQRCode.RapidAntigen

        val registrationData = submissionService.asyncRegisterDeviceViaGUID(request.registrationIdentifier).also {
            Timber.tag(TAG).d("Request %s gave us %s", request, it)
        }

        val testResult = registrationData.testResult.let {
            Timber.tag(TAG).v("Raw test result was %s", it)
            it.toValidatedResult()
        }

        val now = timeStamper.nowUTC

        return RACoronaTest(
            identifier = request.identifier,
            registeredAt = now,
            lastUpdatedAt = now,
            registrationToken = registrationData.registrationToken,
            testResult = testResult,
            testResultReceivedAt = determineReceivedDate(null, testResult),
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

    private fun determineReceivedDate(oldTest: RACoronaTest?, newTestResult: CoronaTestResult): Instant? = when {
        oldTest != null && FINAL_STATES.contains(oldTest.testResult) -> oldTest.testResultReceivedAt
        FINAL_STATES.contains(newTestResult) -> timeStamper.nowUTC
        else -> null
    }

    override suspend fun pollServer(test: CoronaTest): CoronaTest {
        return try {
            Timber.tag(TAG).v("pollServer(test=%s)", test)
            test as RACoronaTest

            if (test.isSubmitted) {
                Timber.tag(TAG).w("Not polling, we have already submitted.")
                return test
            }

            val nowUTC = timeStamper.nowUTC
            val isOlderThan21Days = test.isOlderThan21Days(nowUTC)

            if (isOlderThan21Days && test.testResult == RAT_REDEEMED) {
                Timber.tag(TAG).w("Not polling, test is older than 21 days.")
                return test
            }

            val newTestResult = try {
                submissionService.asyncRequestTestResult(test.registrationToken).let {
                    Timber.tag(TAG).v("Raw test result was %s", it)
                    it.toValidatedResult()
                }
            } catch (e: BadRequestException) {
                if (isOlderThan21Days) {
                    Timber.tag(TAG).w("HTTP 400 error after 21 days, remapping to RAT_REDEEMED.")
                    RAT_REDEEMED
                } else {
                    Timber.tag(TAG).v("Unexpected HTTP 400 error, rethrowing...")
                    throw e
                }
            }

            test.copy(
                testResult = check60Days(test, newTestResult),
                testResultReceivedAt = determineReceivedDate(test, newTestResult),
                lastUpdatedAt = nowUTC,
                lastError = null
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to poll server for  %s", test)
            if (e !is CwaWebException) e.report(ExceptionCategory.INTERNAL)

            test as RACoronaTest
            test.copy(lastError = e)
        }
    }

    // After 60 days, the previously EXPIRED test is deleted from the server, and it may return pending again.
    private fun check60Days(test: CoronaTest, newResult: CoronaTestResult): CoronaTestResult {
        val calculateDays = Duration(test.registeredAt, timeStamper.nowUTC)
        Timber.tag(PCRProcessor.TAG)
            .d("Calculated test age: %d days, newResult=%s", calculateDays.standardDays, newResult)

        return if ((newResult == PCR_OR_RAT_PENDING || newResult == RAT_PENDING) &&
            calculateDays > VerificationServer.TEST_AVAILABLBILITY
        ) {
            Timber.tag(PCRProcessor.TAG).d("$calculateDays is exceeding the test availability.")
            RAT_REDEEMED
        } else {
            newResult
        }
    }

    override suspend fun onRemove(toBeRemoved: CoronaTest) {
        Timber.tag(TAG).v("onRemove(toBeRemoved=%s)", toBeRemoved)
        // Currently nothing to do
    }

    override suspend fun markSubmitted(test: CoronaTest): RACoronaTest {
        Timber.tag(TAG).d("markSubmitted(test=%s)", test)
        test as RACoronaTest

        return test.copy(isSubmitted = true)
    }

    override suspend fun markProcessing(test: CoronaTest, isProcessing: Boolean): CoronaTest {
        Timber.tag(TAG).v("markProcessing(test=%s, isProcessing=%b)", test, isProcessing)
        test as RACoronaTest

        return test.copy(isProcessing = isProcessing)
    }

    override suspend fun markViewed(test: CoronaTest): CoronaTest {
        Timber.tag(TAG).v("markViewed(test=%s)", test)
        test as RACoronaTest

        return test.copy(isViewed = true)
    }

    override suspend fun updateConsent(test: CoronaTest, consented: Boolean): CoronaTest {
        Timber.tag(TAG).v("updateConsent(test=%s, consented=%b)", test, consented)
        test as RACoronaTest

        return test.copy(isAdvancedConsentGiven = consented)
    }

    override suspend fun updateResultNotification(test: CoronaTest, sent: Boolean): CoronaTest {
        Timber.tag(TAG).v("updateResultNotification(test=%s, sent=%b)", test, sent)
        test as RACoronaTest

        return test.copy(isResultAvailableNotificationSent = sent)
    }

    companion object {
        private val FINAL_STATES = setOf(RAT_POSITIVE, RAT_NEGATIVE, RAT_REDEEMED)
        internal const val TAG = "RapidAntigenProcessor"
    }
}

private fun CoronaTestResult.toValidatedResult(): CoronaTestResult {
    val isValid = when (this) {
        PCR_OR_RAT_PENDING,
        RAT_PENDING,
        RAT_NEGATIVE,
        RAT_POSITIVE,
        RAT_INVALID,
        RAT_REDEEMED -> true

        PCR_NEGATIVE,
        PCR_POSITIVE,
        PCR_INVALID,
        PCR_REDEEMED -> false
    }

    return if (isValid) {
        this
    } else {
        Timber.tag(RapidAntigenProcessor.TAG).e("Server returned invalid RapidAntigen testresult $this")
        RAT_INVALID
    }
}
