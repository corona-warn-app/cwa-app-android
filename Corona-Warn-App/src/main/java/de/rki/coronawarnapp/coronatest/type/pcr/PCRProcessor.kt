package de.rki.coronawarnapp.coronatest.type.pcr

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.execution.TestResultScheduler
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
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
        response.testResult.validOrThrow()

        testResultDataCollector.updatePendingTestResultReceivedTime(response.testResult)

        if (response.testResult == CoronaTestResult.PCR_POSITIVE) {
            analyticsKeySubmissionCollector.reportPositiveTestResultReceived()
            deadmanNotificationScheduler.cancelScheduledWork()
        }

        analyticsKeySubmissionCollector.reportTestRegistered()

//        val currentTime = timeStamper.nowUTC
//        submissionSettings.initialTestResultReceivedAt = currentTime
//        testResultReceivedDateFlowInternal.value = currentTime.toDate()
        if (response.testResult == CoronaTestResult.PCR_OR_RAT_PENDING) {
//            riskWorkScheduler.setPeriodicRiskCalculation(enabled = true)

            testResultScheduler.setPeriodicTestPolling(enabled = true)
        }

        return PCRCoronaTest(
            identifier = request.identifier,
            registeredAt = timeStamper.nowUTC,
            registrationToken = response.registrationToken,
            testResult = response.testResult,
        )
    }

    override suspend fun pollServer(test: CoronaTest): CoronaTest {
        return try {
            Timber.tag(TAG).v("pollServer(test=%s)", test)
            test as PCRCoronaTest

            if (test.isSubmitted || test.isSubmissionAllowed) {
                Timber.tag(TAG).w("Not refreshing already final test.")
                return test
            }

            val testResult = submissionService.asyncRequestTestResult(test.registrationToken)
            Timber.tag(TAG).d("Test result was %s", testResult)

            testResult.validOrThrow()

            testResultDataCollector.updatePendingTestResultReceivedTime(testResult)

            if (testResult == CoronaTestResult.PCR_POSITIVE) {
                analyticsKeySubmissionCollector.reportPositiveTestResultReceived()
                deadmanNotificationScheduler.cancelScheduledWork()
            }

            test.copy(
                testResult = testResult,
                lastError = null
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to poll server for  %s", test)
            if (e !is CwaWebException) e.report(ExceptionCategory.INTERNAL)

            test as PCRCoronaTest
            test.copy(lastError = e)
        }
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

        return test.copy(isProcessing = true)
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

    companion object {
        private const val TAG = "PCRProcessor"
    }
}

private fun CoronaTestResult.validOrThrow() {
    val isValid = when (this) {
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

    if (!isValid) throw IllegalArgumentException("Invalid testResult $this")
}
