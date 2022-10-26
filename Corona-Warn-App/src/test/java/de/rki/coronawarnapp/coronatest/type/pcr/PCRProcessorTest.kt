package de.rki.coronawarnapp.coronatest.type.pcr

import de.rki.coronawarnapp.bugreporting.censors.submission.PcrTeleTanCensor
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
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultCollector
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

class PCRProcessorTest : BaseTest() {
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var submissionService: CoronaTestService
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var analyticsTestResultCollector: AnalyticsTestResultCollector

    private val nowUTC = Instant.parse("2021-03-15T05:45:00.000Z")
    private val defaultTest = PCRCoronaTest(
        identifier = "identifier",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = nowUTC,
        registrationToken = "regtoken",
        testResult = PCR_OR_RAT_PENDING
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
                    ),
                )
            }
        }

        analyticsKeySubmissionCollector.apply {
            coEvery { reportRegisteredWithTeleTAN() } just Runs
            coEvery { reset(PCR) } just Runs
            coEvery { reportPositiveTestResultReceived(PCR) } just Runs
            coEvery { reportTestRegistered(PCR) } just Runs
        }
        analyticsTestResultCollector.apply {
            coEvery { reportTestResultReceived(any(), any()) } just Runs
            coEvery { reportTestRegistered(PCR) } just Runs
            coEvery { clear(PCR) } just Runs
        }
    }

    @AfterEach
    fun teardown() {
        runTest { PcrTeleTanCensor.clearTans() }
    }

    fun createInstance() = PCRTestProcessor(
        timeStamper = timeStamper,
        submissionService = submissionService,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        analyticsTestResultCollector = analyticsTestResultCollector
    )

    @Test
    fun `if we receive a pending result 60 days after registration, we map to REDEEMED`() = runTest {
        val instance = createInstance()

        val pcrTest = defaultTest.copy(
            testResult = PCR_POSITIVE,
        )

        instance.pollServer(pcrTest).testResult shouldBe PCR_OR_RAT_PENDING

        val past60DaysTest = pcrTest.copy(
            registeredAt = nowUTC.minus(Duration.ofDays(61))
        )

        instance.pollServer(past60DaysTest).testResult shouldBe PCR_OR_RAT_REDEEMED
    }

    @Test
    fun `registering a new test maps invalid results to INVALID state`() = runTest {
        var registrationData = RegistrationData(
            registrationToken = "regtoken",
            testResultResponse = CoronaTestResultResponse(
                coronaTestResult = PCR_OR_RAT_PENDING,
                sampleCollectedAt = null,
                labId = null,
            )
        )
        coEvery { submissionService.registerTest(any()) } answers { registrationData }

        val instance = createInstance()

        val request = CoronaTestQRCode.PCR(
            qrCodeGUID = "guid",
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
                PCR_OR_RAT_PENDING,
                PCR_NEGATIVE,
                PCR_POSITIVE,
                PCR_INVALID,
                PCR_OR_RAT_REDEEMED -> instance.create(request).testResult shouldBe it

                RAT_PENDING,
                RAT_NEGATIVE,
                RAT_POSITIVE,
                RAT_INVALID,
                RAT_REDEEMED -> instance.create(request).testResult shouldBe PCR_INVALID
            }
        }
    }

    @Test
    fun `polling maps invalid results to INVALID state`() = runTest {
        var pollResult: CoronaTestResult = PCR_OR_RAT_PENDING
        coEvery { submissionService.checkTestResult(any()) } answers {
            CoronaTestResultResponse(
                coronaTestResult = pollResult,
                sampleCollectedAt = null,
                labId = null,
            )
        }

        val instance = createInstance()

        val pcrTest = defaultTest.copy(
            testResult = PCR_POSITIVE,
        )

        values().forEach {
            pollResult = it
            when (it) {
                PCR_OR_RAT_PENDING,
                PCR_NEGATIVE,
                PCR_POSITIVE,
                PCR_INVALID,
                PCR_OR_RAT_REDEEMED -> {
                    Timber.v("Should NOT throw for $it")
                    instance.pollServer(pcrTest).testResult shouldBe it
                }
                RAT_PENDING,
                RAT_NEGATIVE,
                RAT_POSITIVE,
                RAT_INVALID,
                RAT_REDEEMED -> {
                    Timber.v("Should throw for $it")
                    instance.pollServer(pcrTest).testResult shouldBe PCR_INVALID
                }
            }
        }
    }

    // TANs are automatically positive, there is no test result available screen that should be reached
    @Test
    fun `registering a TAN test automatically consumes the notification flag`() = runTest {
        val instance = createInstance()

        instance.create(CoronaTestTAN.PCR(tan = "thisIsATan")).apply {
            isResultAvailableNotificationSent shouldBe true
        }

        instance.create(
            CoronaTestQRCode.PCR(
                qrCodeGUID = "thisIsAQRCodeGUID",
                rawQrCode = "rawQrCode"
            )
        ).apply {
            isResultAvailableNotificationSent shouldBe false
        }
    }

    @Test
    fun `polling is skipped if test is older than 21 days and state was already REDEEMED`() = runTest {
        coEvery { submissionService.checkTestResult(any()) } answers {
            CoronaTestResultResponse(
                coronaTestResult = PCR_POSITIVE,
                sampleCollectedAt = null,
                labId = null,
            )
        }

        val instance = createInstance()

        val pcrTest = defaultTest.copy(
            registeredAt = nowUTC.minus(Duration.ofDays(22)),
            testResult = PCR_OR_RAT_REDEEMED,
        )

        // Older than 21 days and already redeemed
        instance.pollServer(pcrTest) shouldBe pcrTest

        // Older than 21 days but not in final state, we take value from server
        instance.pollServer(
            pcrTest.copy(testResult = PCR_NEGATIVE)
        ).testResult shouldBe PCR_POSITIVE
    }

    @Test
    fun `http 400 errors map to REDEEMED (EXPIRED) state after 21 days`() = runTest {
        val ourBadRequest = BadRequestException("Who?")
        coEvery { submissionService.checkTestResult(any()) } throws ourBadRequest

        val instance = createInstance()

        val pcrTest = defaultTest.copy(
            testResult = PCR_POSITIVE,
        )

        // Test is not older than 21 days, we want the error!
        instance.pollServer(pcrTest).apply {
            testResult shouldBe PCR_POSITIVE
            lastError shouldBe ourBadRequest
        }

        // Test IS older than 21 days, we expected the error, and map it to REDEEMED (expired)
        instance.pollServer(pcrTest.copy(registeredAt = nowUTC.minus(Duration.ofDays(22)))).apply {
            testResult shouldBe PCR_OR_RAT_REDEEMED
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
            CoronaTestQRCode.PCR(
                qrCodeGUID = "guid",
                isDccConsentGiven = true,
                dateOfBirth = LocalDate.parse("2021-06-02"),
                rawQrCode = "rawQrCode"
            )
        ).apply {
            isDccConsentGiven shouldBe true
            isDccDataSetCreated shouldBe false
            isDccSupportedByPoc shouldBe true
            labId shouldBe "labId"
        }

        createInstance().create(
            CoronaTestQRCode.PCR(
                qrCodeGUID = "guid",
                dateOfBirth = LocalDate.parse("2021-06-02"),
                rawQrCode = "rawQrCode"
            )
        ).apply {
            isDccConsentGiven shouldBe false
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
                coronaTestResult = PCR_NEGATIVE,
                labId = "labId",
            )
        )
        coEvery { submissionService.registerTest(any()) } answers { registrationData }

        createInstance().create(
            CoronaTestQRCode.PCR(
                qrCodeGUID = "guid",
                isDccConsentGiven = true,
                dateOfBirth = LocalDate.parse("2021-06-02"),
                rawQrCode = "rawQrCode"
            )
        ).apply {
            testResult shouldBe PCR_NEGATIVE
            labId shouldBe "labId"
            isDccDataSetCreated shouldBe false
            isDccSupportedByPoc shouldBe true
        }
    }

    @Test
    fun `new data received during polling is stored in the test`() = runTest {
        val instance = createInstance()
        val pcrTest = PCRCoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = nowUTC,
            registrationToken = "regtoken",
            testResult = PCR_POSITIVE,
        )

        (instance.pollServer(pcrTest) as PCRCoronaTest).apply {
            labId shouldBe null
        }

        coEvery { submissionService.checkTestResult(any()) } returns CoronaTestResultResponse(
            coronaTestResult = PCR_OR_RAT_PENDING,
            sampleCollectedAt = nowUTC,
            labId = "labId",
        )

        (instance.pollServer(pcrTest) as PCRCoronaTest).apply {
            labId shouldBe "labId"
        }
    }

    @Test
    fun `recycle sets recycledAt`() = runTest {
        val pcrTest = defaultTest.copy(recycledAt = null)

        createInstance().run {
            recycle(pcrTest) shouldBe pcrTest.copy(recycledAt = nowUTC)
        }
    }

    @Test
    fun `restore clears recycledAt`() = runTest {
        val pcrTest = defaultTest.copy(recycledAt = nowUTC)

        createInstance().run {
            restore(pcrTest) shouldBe pcrTest.copy(recycledAt = null)
        }
    }

    @Test
    fun `creates test for Rapid PCR request`() = runTest {
        createRapidPcCRTestData(coronaTestResult = PCR_NEGATIVE).run {
            coEvery { submissionService.registerTest(tokenRequest = serverRequest) } returns registrationData

            createInstance().create(request = request).also {
                it.identifier shouldBe request.identifier
                it.registeredAt shouldBe nowUTC
                it.lastUpdatedAt shouldBe nowUTC
                it.registrationToken shouldBe registrationData.registrationToken
                it.testResult shouldBe resultResponse.coronaTestResult
                it.testResultReceivedAt shouldBe nowUTC
                it.isDccSupportedByPoc shouldBe request.isDccSupportedByPoc
                it.isDccConsentGiven shouldBe request.isDccConsentGiven
                it.labId shouldBe resultResponse.labId
                it.qrCodeHash shouldBe request.rawQrCode.toSHA256()
            }

            val type = request.type
            coVerify {
                analyticsKeySubmissionCollector.reset(type)
                analyticsTestResultCollector.clear(type)
                analyticsTestResultCollector.reportTestRegistered(type)
                analyticsTestResultCollector.reportTestResultReceived(resultResponse.coronaTestResult, type)
            }

            coVerify(exactly = 0) {
                analyticsKeySubmissionCollector.reportPositiveTestResultReceived(type)
            }
        }
    }

    @Test
    fun `reports positive Rapid PCR result`() = runTest {
        createRapidPcCRTestData(coronaTestResult = PCR_POSITIVE).run {
            coEvery { submissionService.registerTest(tokenRequest = serverRequest) } returns registrationData
            createInstance().create(request = request)

            coVerify {
                analyticsKeySubmissionCollector.reportPositiveTestResultReceived(request.type)
            }
        }
    }

    private fun createRapidPcCRTestData(coronaTestResult: CoronaTestResult) = RapidPCRTestData(
        coronaTestResult = coronaTestResult,
        nowUTC = nowUTC
    )

    data class RapidPCRTestData(
        private val coronaTestResult: CoronaTestResult,
        private val nowUTC: Instant
    ) {
        val request = CoronaTestQRCode.RapidPCR(
            dateOfBirth = LocalDate.parse("2022-02-10"),
            isDccConsentGiven = true,
            isDccSupportedByPoc = true,
            rawQrCode = "rawQrCode",
            hash = "hash",
            createdAt = nowUTC
        )

        val serverRequest = RegistrationRequest(
            key = request.registrationIdentifier,
            dateOfBirthKey = null,
            type = VerificationKeyType.GUID
        )

        val resultResponse = CoronaTestResultResponse(
            coronaTestResult = coronaTestResult,
            sampleCollectedAt = nowUTC,
            labId = "labId"
        )

        val registrationData = RegistrationData(
            registrationToken = "registrationData",
            testResultResponse = resultResponse
        )
    }
}
