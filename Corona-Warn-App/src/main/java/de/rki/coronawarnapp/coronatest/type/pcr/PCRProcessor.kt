package de.rki.coronawarnapp.coronatest.type.pcr

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.execution.TestResultScheduler
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
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest.TestResultDataCollector
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Reusable
class PCRProcessor @Inject constructor(
    private val timeStamper: TimeStamper,
    private val submissionService: CoronaTestService,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    private val testResultDataCollector: TestResultDataCollector,
    private val deadmanNotificationScheduler: DeadmanNotificationScheduler,
    private val testResultScheduler: TestResultScheduler,
) : CoronaTestProcessor {

    override val type: CoronaTest.Type = CoronaTest.Type.PCR

    override suspend fun create(request: CoronaTestQRCode): PCRCoronaTest {
        Timber.tag(TAG).d("create(data=%s)", request)
        request as CoronaTestQRCode.PCR

        val registrationData = submissionService.asyncRegisterDeviceViaGUID(request.qrCodeGUID)

        testResultDataCollector.saveTestResultAnalyticsSettings(registrationData.testResult) // This saves received at

        return createCoronaTest(request, registrationData)
    }

    override suspend fun create(request: CoronaTestTAN): CoronaTest {
        Timber.tag(TAG).d("create(data=%s)", request)
        request as CoronaTestTAN.PCR

        val registrationData = submissionService.asyncRegisterDeviceViaTAN(request.tan)

        analyticsKeySubmissionCollector.reportRegisteredWithTeleTAN()

        return createCoronaTest(request, registrationData)
    }

    private suspend fun createCoronaTest(
        request: TestRegistrationRequest,
        response: CoronaTestService.RegistrationData
    ): PCRCoronaTest {
        analyticsKeySubmissionCollector.reset()

        val testResult = response.testResult.validOrThrow()

        testResultDataCollector.updatePendingTestResultReceivedTime(testResult)

        if (testResult == PCR_POSITIVE) {
            analyticsKeySubmissionCollector.reportPositiveTestResultReceived()
            deadmanNotificationScheduler.cancelScheduledWork()
        }

        analyticsKeySubmissionCollector.reportTestRegistered()

        if (testResult == PCR_OR_RAT_PENDING) {
            testResultScheduler.setPeriodicTestPolling(enabled = true)
        }

        return PCRCoronaTest(
            identifier = request.identifier,
            registeredAt = timeStamper.nowUTC,
            registrationToken = response.registrationToken,
            testResult = testResult,
            testResultReceivedAt = determineReceivedDate(null, testResult),
        )
    }

    override suspend fun pollServer(test: CoronaTest): CoronaTest {
        return try {
            Timber.tag(TAG).v("pollServer(test=%s)", test)
            test as PCRCoronaTest

            if (test.isSubmitted) {
                Timber.tag(TAG).w("Not refreshing, we have already submitted.")
                return test
            }

            val newTestResult = submissionService.asyncRequestTestResult(test.registrationToken)
            Timber.tag(TAG).d("Test result was %s", newTestResult)

            newTestResult.validOrThrow()

            testResultDataCollector.updatePendingTestResultReceivedTime(newTestResult)

            if (newTestResult == PCR_POSITIVE) {
                analyticsKeySubmissionCollector.reportPositiveTestResultReceived()
                deadmanNotificationScheduler.cancelScheduledWork()
            }

            test.copy(
                testResult = newTestResult,
                testResultReceivedAt = determineReceivedDate(test, newTestResult),
                lastError = null
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to poll server for  %s", test)
            if (e !is CwaWebException) e.report(ExceptionCategory.INTERNAL)

            test as PCRCoronaTest
            test.copy(lastError = e)
        }
    }

    private fun determineReceivedDate(oldTest: PCRCoronaTest?, newTestResult: CoronaTestResult): Instant? = when {
        oldTest != null && FINAL_STATES.contains(oldTest.testResult) -> oldTest.testResultReceivedAt
        FINAL_STATES.contains(newTestResult) -> timeStamper.nowUTC
        else -> null
    }

    override suspend fun onRemove(toBeRemoved: CoronaTest) {
        Timber.tag(TAG).v("onRemove(toBeRemoved=%s)", toBeRemoved)
        testResultDataCollector.clear()
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

    override suspend fun updateConsent(test: CoronaTest, consented: Boolean): CoronaTest {
        Timber.tag(TAG).v("updateConsent(test=%s, consented=%b)", test, consented)
        test as PCRCoronaTest

        return test.copy(isAdvancedConsentGiven = consented)
    }

    override suspend fun updateResultNotification(test: CoronaTest, sent: Boolean): CoronaTest {
        Timber.tag(TAG).v("updateResultNotification(test=%s, sent=%b)", test, sent)
        test as PCRCoronaTest

        return test.copy(isResultAvailableNotificationSent = sent)
    }

    companion object {
        private val FINAL_STATES = setOf(PCR_POSITIVE, PCR_NEGATIVE, PCR_REDEEMED)
        private const val TAG = "PCRProcessor"
    }
}

private fun CoronaTestResult.validOrThrow(): CoronaTestResult {
    val isValid = when (this) {
        PCR_OR_RAT_PENDING,
        PCR_NEGATIVE,
        PCR_POSITIVE,
        PCR_INVALID,
        PCR_REDEEMED -> true

        RAT_PENDING,
        RAT_NEGATIVE,
        RAT_POSITIVE,
        RAT_INVALID,
        RAT_REDEEMED -> false
    }

    if (!isValid) throw IllegalArgumentException("Invalid testResult $this")
    return this
}
