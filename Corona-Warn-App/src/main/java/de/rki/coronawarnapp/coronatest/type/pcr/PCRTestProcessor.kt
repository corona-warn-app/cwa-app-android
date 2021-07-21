package de.rki.coronawarnapp.coronatest.type.pcr

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.RegistrationData
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.coronatest.type.common.DateOfBirthKey
import de.rki.coronawarnapp.coronatest.type.isOlderThan21Days
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultCollector
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
class PCRTestProcessor @Inject constructor(
    private val timeStamper: TimeStamper,
    private val submissionService: CoronaTestService,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    private val analyticsTestResultCollector: AnalyticsTestResultCollector
) : CoronaTestProcessor {

    override val type: CoronaTest.Type = CoronaTest.Type.PCR

    override suspend fun create(request: TestRegistrationRequest): CoronaTest = when (request) {
        is CoronaTestQRCode.PCR -> createQR(request)
        is CoronaTestTAN.PCR -> createTAN(request)
        else -> throw IllegalArgumentException("PCRProcessor: Unknown test request: $request")
    }

    private suspend fun createQR(request: CoronaTestQRCode.PCR): PCRCoronaTest {
        Timber.tag(TAG).d("createQR(data=%s)", request)

        analyticsKeySubmissionCollector.reset(type)
        analyticsTestResultCollector.clear(type)

        val dateOfBirthKey = if (request.isDccConsentGiven && request.dateOfBirth != null) {
            DateOfBirthKey(request.qrCodeGUID, request.dateOfBirth)
        } else null

        val serverRequest = RegistrationRequest(
            key = request.qrCodeGUID,
            dateOfBirthKey = dateOfBirthKey,
            type = VerificationKeyType.GUID,
        )

        val registrationData = submissionService.registerTest(serverRequest).also {
            Timber.tag(TAG).d("Request %s gave us %s", request, it)
        }

        return createCoronaTest(request, registrationData)
    }

    private suspend fun createTAN(request: CoronaTestTAN.PCR): CoronaTest {
        Timber.tag(TAG).d("createTAN(data=%s)", request)

        analyticsKeySubmissionCollector.reset(type)
        analyticsTestResultCollector.clear(type)

        val serverRequest = RegistrationRequest(
            key = request.tan,
            dateOfBirthKey = null,
            type = VerificationKeyType.TELETAN,
        )

        val registrationData = submissionService.registerTest(serverRequest)

        analyticsKeySubmissionCollector.reportRegisteredWithTeleTAN()

        return createCoronaTest(request, registrationData).copy(
            isResultAvailableNotificationSent = true
        )
    }

    private suspend fun createCoronaTest(
        request: TestRegistrationRequest,
        response: RegistrationData
    ): PCRCoronaTest {

        val testResult = response.testResultResponse.coronaTestResult.let {
            Timber.tag(TAG).v("Raw test result $it")

            it.toValidatedResult()
        }

        analyticsKeySubmissionCollector.reportTestRegistered(type)
        if (testResult == PCR_POSITIVE) {
            analyticsKeySubmissionCollector.reportPositiveTestResultReceived(type)
        }

        // only collect for QR code test
        if (request is CoronaTestQRCode) {
            analyticsTestResultCollector.reportTestRegistered(type)
            analyticsTestResultCollector.reportTestResultReceived(testResult, type)
        }

        val now = timeStamper.nowUTC

        return PCRCoronaTest(
            identifier = request.identifier,
            registeredAt = now,
            lastUpdatedAt = now,
            registrationToken = response.registrationToken,
            testResult = testResult,
            testResultReceivedAt = determineReceivedDate(null, testResult),
            isDccConsentGiven = request.isDccConsentGiven,
            labId = response.testResultResponse.labId
        )
    }

    override suspend fun pollServer(test: CoronaTest): CoronaTest {
        return try {
            Timber.tag(TAG).v("pollServer(test=%s)", test)
            test as PCRCoronaTest

            if (test.isSubmitted) {
                Timber.tag(TAG).w("Not polling, we have already submitted.")
                return test
            }

            val nowUTC = timeStamper.nowUTC
            val isOlderThan21Days = test.isOlderThan21Days(nowUTC)

            if (isOlderThan21Days && test.testResult == PCR_OR_RAT_REDEEMED) {
                Timber.tag(TAG).w("Not polling, test is older than 21 days.")
                return test
            }

            val response = try {
                submissionService.checkTestResult(test.registrationToken)
                    .also {
                        Timber.tag(TAG).d("Raw test result was %s", it)
                    }
                    .let { orig ->
                        orig.copy(coronaTestResult = orig.coronaTestResult.toValidatedResult())
                    }
            } catch (e: BadRequestException) {
                if (isOlderThan21Days) {
                    Timber.tag(TAG).w("HTTP 400 error after 21 days, remapping to PCR_OR_RAT_REDEEMED.")
                    CoronaTestResultResponse(coronaTestResult = PCR_OR_RAT_REDEEMED)
                } else {
                    Timber.tag(TAG).v("Unexpected HTTP 400 error, rethrowing...")
                    throw e
                }
            }

            if (response.coronaTestResult == PCR_POSITIVE) {
                analyticsKeySubmissionCollector.reportPositiveTestResultReceived(type)
            }
            analyticsTestResultCollector.reportTestResultReceived(response.coronaTestResult, type)

            test.copy(
                testResult = check60Days(test, response.coronaTestResult),
                testResultReceivedAt = determineReceivedDate(test, response.coronaTestResult),
                lastUpdatedAt = nowUTC,
                labId = response.labId ?: test.labId,
                lastError = null,
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to poll server for  %s", test)
            if (e !is CwaWebException) e.report(ExceptionCategory.INTERNAL)

            test as PCRCoronaTest
            test.copy(lastError = e)
        }
    }

    // After 60 days, the previously EXPIRED test is deleted from the server, and it may return pending again.
    private fun check60Days(test: CoronaTest, newResult: CoronaTestResult): CoronaTestResult {
        val calculateDays = Duration(test.registeredAt, timeStamper.nowUTC)
        Timber.tag(TAG).d("Calculated test age: %d days, newResult=%s", calculateDays.standardDays, newResult)

        return if (newResult == PCR_OR_RAT_PENDING && calculateDays > VerificationServer.TEST_AVAILABLBILITY) {
            Timber.tag(TAG).d("$calculateDays is exceeding the test availability.")
            PCR_OR_RAT_REDEEMED
        } else {
            newResult
        }
    }

    private fun determineReceivedDate(oldTest: PCRCoronaTest?, newTestResult: CoronaTestResult): Instant? = when {
        oldTest != null && FINAL_STATES.contains(oldTest.testResult) -> oldTest.testResultReceivedAt
        FINAL_STATES.contains(newTestResult) -> timeStamper.nowUTC
        else -> null
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

        return test.copy(isProcessing = isProcessing)
    }

    override suspend fun markViewed(test: CoronaTest): CoronaTest {
        Timber.tag(TAG).v("markViewed(test=%s)", test)
        test as PCRCoronaTest

        return test.copy(isViewed = true)
    }

    override suspend fun updateSubmissionConsent(test: CoronaTest, consented: Boolean): CoronaTest {
        Timber.tag(TAG).v("updateSubmissionConsent(test=%s, consented=%b)", test, consented)
        test as PCRCoronaTest

        return test.copy(isAdvancedConsentGiven = consented)
    }

    override suspend fun updateResultNotification(test: CoronaTest, sent: Boolean): CoronaTest {
        Timber.tag(TAG).v("updateResultNotification(test=%s, sent=%b)", test, sent)
        test as PCRCoronaTest

        return test.copy(isResultAvailableNotificationSent = sent)
    }

    override suspend fun markDccCreated(test: CoronaTest, created: Boolean): CoronaTest {
        Timber.tag(TAG).v("markDccCreated(test=%s, created=%b)", test, created)
        test as PCRCoronaTest

        return test.copy(isDccDataSetCreated = created)
    }

    companion object {
        private val FINAL_STATES = setOf(PCR_POSITIVE, PCR_NEGATIVE, PCR_OR_RAT_REDEEMED)
        internal const val TAG = "PCRTestProcessor"
    }
}

private fun CoronaTestResult.toValidatedResult(): CoronaTestResult {
    val isValid = when (this) {
        PCR_OR_RAT_PENDING,
        PCR_NEGATIVE,
        PCR_POSITIVE,
        PCR_INVALID,
        PCR_OR_RAT_REDEEMED -> true

        RAT_PENDING,
        RAT_NEGATIVE,
        RAT_POSITIVE,
        RAT_INVALID,
        RAT_REDEEMED -> false
    }

    return if (isValid) {
        this
    } else {
        Timber.tag(PCRTestProcessor.TAG).e("Server returned invalid PCR testresult $this")
        PCR_INVALID
    }
}
