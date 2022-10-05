package de.rki.coronawarnapp.coronatest.type.rapidantigen

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
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.values
import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.RegistrationData
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultCollector
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

class RAProcessorTest : BaseTest() {

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
                labId = null,
            )

            coEvery { registerTest(any()) } answers {
                val request = arg<RegistrationRequest>(0)

                RegistrationData(
                    registrationToken = "regtoken-${request.type}",
                    testResultResponse = CoronaTestResultResponse(
                        coronaTestResult = PCR_OR_RAT_PENDING,
                        sampleCollectedAt = null,
                        labId = null,
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
            coEvery { reportTestResultReceived(any(), RAPID_ANTIGEN) } just Runs
            coEvery { reportTestRegistered(RAPID_ANTIGEN) } just Runs
            coEvery { clear(RAPID_ANTIGEN) } just Runs
        }
    }

    fun createInstance() = RATestProcessor(
        timeStamper = timeStamper,
        submissionService = submissionService,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        analyticsTestResultCollector = analyticsTestResultCollector,
    )

    @Test
    fun `if we receive a pending result 60 days after registration, we map to REDEEMED`() = runTest {
        val instance = createInstance()

        val raTest = defaultTest.copy(
            testResult = RAT_POSITIVE,
        )

        instance.pollServer(raTest).testResult shouldBe PCR_OR_RAT_PENDING

        val past60DaysTest = raTest.copy(
            registeredAt = nowUTC.minus(Duration.ofDays(61))
        )

        instance.pollServer(past60DaysTest).testResult shouldBe RAT_REDEEMED
    }

    @Test
    fun `registering a new test`() = runTest {
        val request = CoronaTestQRCode.RapidAntigen(
            hash = "hash",
            createdAt = Instant.EPOCH,
            rawQrCode = "rawQrCode"
        )

        val instance = createInstance()
        instance.create(request)

        val expectedServerRequest = RegistrationRequest(
            key = request.registrationIdentifier,
            type = VerificationKeyType.GUID,
            dateOfBirthKey = null,
        )

        coVerify {
            submissionService.registerTest(expectedServerRequest)
        }
    }

    @Test
    fun `registering a new test maps invalid results to INVALID state`() = runTest {
        var registrationData = RegistrationData(
            registrationToken = "regtoken",
            testResultResponse = CoronaTestResultResponse(
                coronaTestResult = PCR_OR_RAT_PENDING,
                sampleCollectedAt = null,
                labId = null,
            ),
        )
        coEvery { submissionService.registerTest(any()) } answers { registrationData }

        val instance = createInstance()

        val request = CoronaTestQRCode.RapidAntigen(
            hash = "hash",
            createdAt = Instant.EPOCH,
            rawQrCode = "rawQrCode"
        )

        values().forEach {
            registrationData = registrationData.copy(
                testResultResponse = CoronaTestResultResponse(
                    coronaTestResult = it,
                    sampleCollectedAt = null,
                    labId = null,
                )
            )
            when (it) {
                PCR_NEGATIVE,
                PCR_POSITIVE,
                PCR_INVALID -> instance.create(request).testResult shouldBe RAT_INVALID

                PCR_OR_RAT_PENDING,
                PCR_OR_RAT_REDEEMED,
                RAT_PENDING,
                RAT_NEGATIVE,
                RAT_POSITIVE,
                RAT_INVALID,
                RAT_REDEEMED -> instance.create(request).testResult shouldBe it
            }
        }
    }

    @Test
    fun `polling filters out invalid test result values`() = runTest {
        var pollResult: CoronaTestResult = PCR_OR_RAT_PENDING
        coEvery { submissionService.checkTestResult(any()) } answers {
            CoronaTestResultResponse(
                coronaTestResult = pollResult,
                sampleCollectedAt = null,
                labId = null,
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
                PCR_INVALID -> instance.pollServer(raTest).testResult shouldBe RAT_INVALID

                PCR_OR_RAT_PENDING,
                PCR_OR_RAT_REDEEMED,
                RAT_PENDING,
                RAT_NEGATIVE,
                RAT_POSITIVE,
                RAT_INVALID,
                RAT_REDEEMED -> instance.pollServer(raTest).testResult shouldBe it
            }
        }
    }

    @Test
    fun `polling is skipped if test is older than 21 days and state was already REDEEMED`() = runTest {
        coEvery { submissionService.checkTestResult(any()) } answers {
            CoronaTestResultResponse(
                coronaTestResult = RAT_POSITIVE,
                sampleCollectedAt = null,
                labId = null,
            )
        }

        val instance = createInstance()

        val raTest = defaultTest.copy(
            registeredAt = nowUTC.minus(Duration.ofDays(22)),
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
    fun `http 400 errors map to REDEEMED (EXPIRED) state after 21 days`() = runTest {
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
        instance.pollServer(raTest.copy(registeredAt = nowUTC.minus(Duration.ofDays(22)))).apply {
            testResult shouldBe RAT_REDEEMED
            lastError shouldBe null
        }
    }

    @Test
    fun `giving submission consent`() = runTest {
        val instance = createInstance()

        instance.updateSubmissionConsent(defaultTest, true) shouldBe defaultTest.copy(
            isAdvancedConsentGiven = true
        )
        instance.updateSubmissionConsent(defaultTest, false) shouldBe defaultTest.copy(
            isAdvancedConsentGiven = false
        )
    }

    @Test
    fun `request parameters for dcc are mapped`() = runTest {
        val registrationData = RegistrationData(
            registrationToken = "regtoken",
            testResultResponse = CoronaTestResultResponse(
                coronaTestResult = PCR_OR_RAT_PENDING,
                sampleCollectedAt = null,
                labId = "labId",
            )
        )
        coEvery { submissionService.registerTest(any()) } answers { registrationData }

        createInstance().create(
            CoronaTestQRCode.RapidAntigen(
                hash = "hash",
                createdAt = Instant.EPOCH,
                isDccConsentGiven = false,
                dateOfBirth = LocalDate.parse("2021-06-02"),
                isDccSupportedByPoc = false,
                rawQrCode = "rawQrCode"
            )
        ).apply {
            isDccConsentGiven shouldBe false
            isDccDataSetCreated shouldBe false
            isDccSupportedByPoc shouldBe false
            labId shouldBe "labId"
        }

        createInstance().create(
            CoronaTestQRCode.RapidAntigen(
                hash = "hash",
                createdAt = Instant.EPOCH,
                isDccConsentGiven = true,
                dateOfBirth = LocalDate.parse("2021-06-02"),
                isDccSupportedByPoc = true,
                rawQrCode = "rawQrCode"
            )
        ).apply {
            isDccConsentGiven shouldBe true
            isDccDataSetCreated shouldBe false
            isDccSupportedByPoc shouldBe true
            labId shouldBe "labId"
        }
    }

    @Test
    fun `marking dcc as created`() = runTest {
        val instance = createInstance()

        instance.markDccCreated(defaultTest, true) shouldBe defaultTest.copy(
            isDccDataSetCreated = true
        )
        instance.markDccCreated(defaultTest, false) shouldBe defaultTest.copy(
            isDccDataSetCreated = false
        )
    }

    @Test
    fun `response parameters are stored during initial registration`() = runTest {
        val registrationData = RegistrationData(
            registrationToken = "regtoken",
            testResultResponse = CoronaTestResultResponse(
                coronaTestResult = RAT_NEGATIVE,
                sampleCollectedAt = Instant.ofEpochSecond(123),
                labId = "labId",
            )
        )
        coEvery { submissionService.registerTest(any()) } answers { registrationData }

        createInstance().create(
            CoronaTestQRCode.RapidAntigen(
                hash = "hash",
                createdAt = Instant.EPOCH,
                dateOfBirth = LocalDate.parse("2021-06-02"),
                rawQrCode = "rawQrCode"
            )
        ).apply {
            this as RACoronaTest
            testResult shouldBe RAT_NEGATIVE
            sampleCollectedAt shouldBe Instant.ofEpochSecond(123)
            labId shouldBe "labId"
        }
    }

    @Test
    fun `new data received during polling is stored in the test`() = runTest {
        val instance = createInstance()
        val raTest = RACoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = nowUTC,
            registrationToken = "regtoken",
            testResult = RAT_POSITIVE,
            testedAt = Instant.EPOCH,
        )

        (instance.pollServer(raTest) as RACoronaTest).apply {
            sampleCollectedAt shouldBe null
            labId shouldBe null
        }

        coEvery { submissionService.checkTestResult(any()) } returns CoronaTestResultResponse(
            coronaTestResult = PCR_OR_RAT_PENDING,
            sampleCollectedAt = nowUTC,
            labId = "labId",
        )

        (instance.pollServer(raTest) as RACoronaTest).apply {
            sampleCollectedAt shouldBe nowUTC
            labId shouldBe "labId"
        }
    }

    @Test
    fun `recycle sets recycledAt`() = runTest {
        val raTest = defaultTest.copy(recycledAt = null)

        createInstance().run {
            recycle(raTest) shouldBe raTest.copy(recycledAt = nowUTC)
        }
    }

    @Test
    fun `restore clears recycledAt`() = runTest {
        val raTest = defaultTest.copy(recycledAt = nowUTC)

        createInstance().run {
            restore(raTest) shouldBe raTest.copy(recycledAt = null)
        }
    }
}
