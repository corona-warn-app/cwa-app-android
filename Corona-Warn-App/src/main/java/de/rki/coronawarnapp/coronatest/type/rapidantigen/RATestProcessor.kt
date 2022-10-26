package de.rki.coronawarnapp.coronatest.type.rapidantigen

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.isOlderThan21Days
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultCollector
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeStamper
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@Reusable
class RATestProcessor @Inject constructor(
    private val timeStamper: TimeStamper,
    private val submissionService: CoronaTestService,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    private val analyticsTestResultCollector: AnalyticsTestResultCollector,
) : PersonalCoronaTestProcessor {

    override val type: BaseCoronaTest.Type = BaseCoronaTest.Type.RAPID_ANTIGEN

    override suspend fun create(request: TestRegistrationRequest): PersonalCoronaTest = when (request) {
        is CoronaTestQRCode.RapidAntigen -> createQR(request)
        else -> throw IllegalArgumentException("RAProcessor: Unknown test request: $request")
    }

    private suspend fun createQR(request: CoronaTestQRCode.RapidAntigen): RACoronaTest {
        Timber.tag(TAG).d("createQR(data=%s)", request)

        analyticsKeySubmissionCollector.reset(type)
        analyticsTestResultCollector.clear(type)

        val serverRequest = RegistrationRequest(
            key = request.registrationIdentifier,
            dateOfBirthKey = null,
            type = VerificationKeyType.GUID
        )

        val registrationData = submissionService.registerTest(serverRequest).also {
            Timber.tag(TAG).d("Request %s gave us %s", request, it)
        }

        val testResult = registrationData.testResultResponse.coronaTestResult.let {
            Timber.tag(TAG).v("Raw test result was %s", it)
            it.toValidatedRaResult()
        }

        analyticsKeySubmissionCollector.reportTestRegistered(type)
        if (testResult == RAT_POSITIVE) {
            analyticsKeySubmissionCollector.reportPositiveTestResultReceived(type)
        }
        analyticsTestResultCollector.reportTestRegistered(type)
        analyticsTestResultCollector.reportTestResultReceived(testResult, type)

        val sampleCollectedAt = registrationData.testResultResponse.sampleCollectedAt

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
            sampleCollectedAt = sampleCollectedAt,
            isDccSupportedByPoc = request.isDccSupportedByPoc,
            isDccConsentGiven = request.isDccConsentGiven,
            labId = registrationData.testResultResponse.labId,
            qrCodeHash = request.rawQrCode.toSHA256()
        )
    }

    private fun determineReceivedDate(oldTest: RACoronaTest?, newTestResult: CoronaTestResult): Instant? = when {
        oldTest != null && FINAL_STATES.contains(oldTest.testResult) -> oldTest.testResultReceivedAt
        FINAL_STATES.contains(newTestResult) -> timeStamper.nowUTC
        else -> null
    }

    override suspend fun pollServer(test: PersonalCoronaTest): PersonalCoronaTest {
        return try {
            Timber.tag(TAG).v("pollServer(test=%s)", test.identifier)
            test as RACoronaTest

            if (test.isSubmitted) {
                Timber.tag(TAG).w("Not polling, we have already submitted.")
                return test
            }

            val nowUTC = timeStamper.nowUTC
            val isOlderThan21Days = test.isOlderThan21Days(nowUTC)

            if (isOlderThan21Days && (test.testResult == RAT_REDEEMED || test.testResult == PCR_OR_RAT_REDEEMED)) {
                Timber.tag(TAG).w("Not polling, test is older than 21 days.")
                return test
            }

            val response = try {
                submissionService.checkTestResult(test.registrationToken).let {
                    Timber.tag(TAG).v("Raw test result was %s", it)
                    it.copy(
                        coronaTestResult = it.coronaTestResult.toValidatedRaResult()
                    )
                }
            } catch (e: BadRequestException) {
                if (isOlderThan21Days) {
                    Timber.tag(TAG).w("HTTP 400 error after 21 days, remapping to RAT_REDEEMED.")
                    CoronaTestResultResponse(coronaTestResult = RAT_REDEEMED)
                } else {
                    Timber.tag(TAG).v("Unexpected HTTP 400 error, rethrowing...")
                    throw e
                }
            }

            if (response.coronaTestResult == RAT_POSITIVE) {
                analyticsKeySubmissionCollector.reportPositiveTestResultReceived(type)
            }
            analyticsTestResultCollector.reportTestResultReceived(response.coronaTestResult, type)

            test.copy(
                testResult = check60DaysRAT(test, response.coronaTestResult, timeStamper.nowUTC),
                testResultReceivedAt = determineReceivedDate(test, response.coronaTestResult),
                lastUpdatedAt = nowUTC,
                sampleCollectedAt = response.sampleCollectedAt ?: test.sampleCollectedAt,
                labId = response.labId ?: test.labId,
                lastError = null
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to poll server for  %s", test.identifier)
            if (e !is CwaWebException) e.report(ExceptionCategory.INTERNAL)

            test as RACoronaTest
            test.copy(lastError = e)
        }
    }

    override suspend fun updateAuthCode(test: PersonalCoronaTest, authCode: String): PersonalCoronaTest {
        Timber.tag(TAG).v("updateAuthCode(test=%s)", test.identifier)
        test as RACoronaTest

        return test.copy(authCode = authCode)
    }

    override suspend fun markSubmitted(test: PersonalCoronaTest): RACoronaTest {
        Timber.tag(TAG).d("markSubmitted(test=%s)", test.identifier)
        test as RACoronaTest

        return test.copy(isSubmitted = true)
    }

    override suspend fun markProcessing(test: PersonalCoronaTest, isProcessing: Boolean): PersonalCoronaTest {
        Timber.tag(TAG).v("markProcessing(test=%s, isProcessing=%b)", test.identifier, isProcessing)
        test as RACoronaTest

        return test.copy(isProcessing = isProcessing)
    }

    override suspend fun markViewed(test: PersonalCoronaTest): PersonalCoronaTest {
        Timber.tag(TAG).v("markViewed(test=%s)", test.identifier)
        test as RACoronaTest

        return test.copy(isViewed = true)
    }

    override suspend fun markBadgeAsViewed(test: PersonalCoronaTest): PersonalCoronaTest {
        Timber.tag(TAG).v("markBadgeAsViewed(test=%s)", test.identifier)
        test as RACoronaTest

        return test.copy(didShowBadge = true)
    }

    override suspend fun updateSubmissionConsent(test: PersonalCoronaTest, consented: Boolean): PersonalCoronaTest {
        Timber.tag(TAG).v("updateSubmissionConsent(test=%s, consented=%b)", test.identifier, consented)
        test as RACoronaTest

        return test.copy(isAdvancedConsentGiven = consented)
    }

    override suspend fun updateResultNotification(test: PersonalCoronaTest, sent: Boolean): PersonalCoronaTest {
        Timber.tag(TAG).v("updateResultNotification(test=%s, sent=%b)", test.identifier, sent)
        test as RACoronaTest

        return test.copy(isResultAvailableNotificationSent = sent)
    }

    override suspend fun markDccCreated(test: PersonalCoronaTest, created: Boolean): PersonalCoronaTest {
        Timber.tag(TAG).v("markDccCreated(test=%s, created=%b)", test.identifier, created)
        test as RACoronaTest

        return test.copy(isDccDataSetCreated = created)
    }

    override suspend fun recycle(test: PersonalCoronaTest): PersonalCoronaTest {
        Timber.tag(TAG).v("recycle(test=%s)", test.identifier)
        test as RACoronaTest

        return test.copy(recycledAt = timeStamper.nowUTC)
    }

    override suspend fun restore(test: PersonalCoronaTest): PersonalCoronaTest {
        Timber.tag(TAG).v("restore(test=%s)", test.identifier)
        test as RACoronaTest

        return test.copy(recycledAt = null)
    }

    companion object {
        private val FINAL_STATES = setOf(RAT_POSITIVE, RAT_NEGATIVE, RAT_REDEEMED, PCR_OR_RAT_REDEEMED)
        internal const val TAG = "RATestProcessor"
    }
}

fun CoronaTestResult.toValidatedRaResult(): CoronaTestResult {
    val isValid = when (this) {
        PCR_OR_RAT_PENDING,
        PCR_OR_RAT_REDEEMED,
        RAT_PENDING,
        RAT_NEGATIVE,
        RAT_POSITIVE,
        RAT_INVALID,
        RAT_REDEEMED -> true

        PCR_NEGATIVE,
        PCR_POSITIVE,
        PCR_INVALID -> false
    }

    return if (isValid) {
        this
    } else {
        Timber.tag(RATestProcessor.TAG).e("Server returned invalid RapidAntigen test result =%s", this)
        RAT_INVALID
    }
}

// After 60 days, the previously EXPIRED test is deleted from the server, and it may return pending again.
fun check60DaysRAT(test: BaseCoronaTest, newResult: CoronaTestResult, now: Instant): CoronaTestResult {
    val testAge = Duration.between(test.registeredAt, now)
    Timber.tag(RATestProcessor.TAG).d("Calculated test age: %d days, newResult=%s", testAge.toDays(), newResult)

    return if ((newResult == PCR_OR_RAT_PENDING || newResult == RAT_PENDING) &&
        testAge > VerificationServer.TestAvailabilityDuration
    ) {
        Timber.tag(RATestProcessor.TAG).d("%s is exceeding the test availability.", testAge)
        RAT_REDEEMED
    } else {
        newResult
    }
}
