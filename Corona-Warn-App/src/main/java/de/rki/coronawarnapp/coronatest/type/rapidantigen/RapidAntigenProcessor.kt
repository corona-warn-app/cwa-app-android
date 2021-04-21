package de.rki.coronawarnapp.coronatest.type.rapidantigen

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
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
import de.rki.coronawarnapp.exception.ExceptionCategory
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
    private val appConfigProvider: AppConfigProvider
) : CoronaTestProcessor {

    override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN

    override suspend fun create(request: CoronaTestQRCode): RACoronaTest {
        Timber.tag(TAG).d("create(data=%s)", request)
        request as CoronaTestQRCode.RapidAntigen

        val registrationData = submissionService.asyncRegisterDeviceViaGUID(request.registrationIdentifier)

        val testResult = registrationData.testResult.validOrThrow()

        val testOutdatedAfter = Duration.standardHours(
            appConfigProvider.getAppConfig()
                .coronaTestParameters.coronaRapidAntigenTestParameters.hoursToDeemTestOutdated
        )

        return RACoronaTest(
            identifier = request.identifier,
            registeredAt = timeStamper.nowUTC,
            registrationToken = registrationData.registrationToken,
            testResult = testResult,
            testResultReceivedAt = determineReceivedDate(null, testResult),
            testedAt = request.createdAt,
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
            outdatedAfter = testOutdatedAfter
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
                Timber.tag(TAG).w("Not refreshing, we have already submitted.")
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

            test as RACoronaTest
            test.copy(lastError = e)
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
        private const val TAG = "RapidAntigenProcessor"
    }
}

private fun CoronaTestResult.validOrThrow(): CoronaTestResult {
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

    if (!isValid) throw IllegalArgumentException("Invalid testResult $this")
    return this
}
