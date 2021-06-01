package de.rki.coronawarnapp.coronatest.type.rapidantigen

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
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.values
import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.RegistrationData
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultCollector
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RapidAntigenProcessorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var submissionService: CoronaTestService
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var analyticsTestResultCollector: AnalyticsTestResultCollector

    private val nowUTC = Instant.parse("2021-03-15T05:45:00.000Z")

    private val defaultTest = RACoronaTest(
        identifier = "identifier",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = nowUTC,
        registrationToken = "regtoken",
        testResult = RAT_PENDING,
        testedAt = Instant.EPOCH,
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns nowUTC

        submissionService.apply {
            coEvery { checkTestResult(any()) } returns CoronaTestResultResponse(
                coronaTestResult = PCR_OR_RAT_PENDING,
                sampleCollectedAt = null,
            )

            coEvery { registerTest(any()) } answers {
                val request = arg<RegistrationRequest>(0)

                RegistrationData(
                    registrationToken = "regtoken-${request.type}",
                    testResultResponse = CoronaTestResultResponse(
                        coronaTestResult = PCR_OR_RAT_PENDING,
                        sampleCollectedAt = null,
                    )
                )
            }
        }

        analyticsKeySubmissionCollector.apply {
            coEvery { reportRegisteredWithTeleTAN() } just Runs
            coEvery { reset(RAPID_ANTIGEN) } just Runs
            coEvery { reportPositiveTestResultReceived(RAPID_ANTIGEN) } just Runs
            coEvery { reportTestRegistered(RAPID_ANTIGEN) } just Runs
        }

        analyticsTestResultCollector.apply {
            coEvery { saveTestResult(any(), RAPID_ANTIGEN) } just Runs
            coEvery { updatePendingTestResultReceivedTime(any(), RAPID_ANTIGEN) } just Runs
            coEvery { reportTestRegistered(RAPID_ANTIGEN) } just Runs
            coEvery { clear(RAPID_ANTIGEN) } just Runs
        }
    }

    fun createInstance() = RAProcessor(
        timeStamper = timeStamper,
        submissionService = submissionService,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        analyticsTestResultCollector = analyticsTestResultCollector,
    )

    @Test
    fun `if a test result poll returns a sc set it on the test`() = runBlockingTest {
        val instance = createInstance()
        val raTest = RACoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = nowUTC,
            registrationToken = "regtoken",
            testResult = RAT_POSITIVE,
            testedAt = Instant.EPOCH,
        )

        (instance.pollServer(raTest) as RACoronaTest).sampleCollectedAt shouldBe null

        coEvery { submissionService.checkTestResult(any()) } returns CoronaTestResultResponse(
            coronaTestResult = PCR_OR_RAT_PENDING,
            sampleCollectedAt = nowUTC,
        )

        (instance.pollServer(raTest) as RACoronaTest).sampleCollectedAt shouldBe nowUTC
    }

    @Test
    fun `if we receive a pending result 60 days after registration, we map to REDEEMED`() = runBlockingTest {
        val instance = createInstance()

        val raTest = defaultTest.copy(
            testResult = RAT_POSITIVE,
        )

        instance.pollServer(raTest).testResult shouldBe PCR_OR_RAT_PENDING

        val past60DaysTest = raTest.copy(
            registeredAt = nowUTC.minus(Duration.standardDays(61))
        )

        instance.pollServer(past60DaysTest).testResult shouldBe RAT_REDEEMED
    }

    @Test
    fun `registering a new test maps invalid results to INVALID state`() = runBlockingTest {
        var registrationData = RegistrationData(
            registrationToken = "regtoken",
            testResultResponse = CoronaTestResultResponse(
                coronaTestResult = PCR_OR_RAT_PENDING,
                sampleCollectedAt = null,
            ),
        )
        coEvery { submissionService.registerTest(any()) } answers { registrationData }

        val instance = createInstance()

        val request = CoronaTestQRCode.RapidAntigen(
            hash = "hash",
            createdAt = Instant.EPOCH,
        )

        values().forEach {
            registrationData = registrationData.copy(
                testResultResponse = CoronaTestResultResponse(
                    coronaTestResult = it,
                    sampleCollectedAt = null,
                )
            )
            when (it) {
                PCR_NEGATIVE,
                PCR_POSITIVE,
                PCR_INVALID,
                PCR_REDEEMED -> instance.create(request).testResult shouldBe RAT_INVALID

                PCR_OR_RAT_PENDING,
                RAT_PENDING,
                RAT_NEGATIVE,
                RAT_POSITIVE,
                RAT_INVALID,
                RAT_REDEEMED -> instance.create(request).testResult shouldBe it
            }
        }
    }

    @Test
    fun `polling filters out invalid test result values`() = runBlockingTest {
        var pollResult: CoronaTestResult = PCR_OR_RAT_PENDING
        coEvery { submissionService.checkTestResult(any()) } answers {
            CoronaTestResultResponse(
                coronaTestResult = pollResult,
                sampleCollectedAt = null,
            )
        }

        val instance = createInstance()

        val raTest = defaultTest.copy(
            testResult = RAT_POSITIVE,
        )

        values().forEach {
            pollResult = it
            when (it) {
                PCR_NEGATIVE,
                PCR_POSITIVE,
                PCR_INVALID,
                PCR_REDEEMED -> instance.pollServer(raTest).testResult shouldBe RAT_INVALID

                PCR_OR_RAT_PENDING,
                RAT_PENDING,
                RAT_NEGATIVE,
                RAT_POSITIVE,
                RAT_INVALID,
                RAT_REDEEMED -> instance.pollServer(raTest).testResult shouldBe it
            }
        }
    }

    @Test
    fun `polling is skipped if test is older than 21 days and state was already REDEEMED`() = runBlockingTest {
        coEvery { submissionService.checkTestResult(any()) } answers {
            CoronaTestResultResponse(
                coronaTestResult = RAT_POSITIVE,
                sampleCollectedAt = null,
            )
        }

        val instance = createInstance()

        val raTest = defaultTest.copy(
            registeredAt = nowUTC.minus(Duration.standardDays(22)),
            testResult = RAT_REDEEMED,
        )

        // Older than 21 days and already redeemed
        instance.pollServer(raTest) shouldBe raTest

        // Older than 21 days but not in final state, we take value from server
        instance.pollServer(
            raTest.copy(testResult = RAT_NEGATIVE)
        ).testResult shouldBe RAT_POSITIVE
    }

    @Test
    fun `http 400 errors map to REDEEMED (EXPIRED) state after 21 days`() = runBlockingTest {
        val ourBadRequest = BadRequestException("Who?")
        coEvery { submissionService.checkTestResult(any()) } throws ourBadRequest

        val instance = createInstance()

        val raTest = RACoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = nowUTC,
            registrationToken = "regtoken",
            testResult = RAT_POSITIVE,
            testedAt = Instant.EPOCH,
        )

        // Test is not older than 21 days, we want the error!
        instance.pollServer(raTest).apply {
            testResult shouldBe RAT_POSITIVE
            lastError shouldBe ourBadRequest
        }

        // Test IS older than 21 days, we expected the error, and map it to REDEEMED (expired)
        instance.pollServer(raTest.copy(registeredAt = nowUTC.minus(Duration.standardDays(22)))).apply {
            testResult shouldBe RAT_REDEEMED
            lastError shouldBe null
        }
    }

    @Test
    fun `giving submission consent`() = runBlockingTest {
        val instance = createInstance()

        instance.updateSubmissionConsent(defaultTest, true) shouldBe defaultTest.copy(
            isAdvancedConsentGiven = true
        )
        instance.updateSubmissionConsent(defaultTest, false) shouldBe defaultTest.copy(
            isAdvancedConsentGiven = false
        )
    }
}
