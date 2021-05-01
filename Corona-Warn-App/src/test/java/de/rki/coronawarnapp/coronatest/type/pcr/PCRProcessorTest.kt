package de.rki.coronawarnapp.coronatest.type.pcr

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
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest.TestResultDataCollector
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
import timber.log.Timber

class PCRProcessorTest : BaseTest() {
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var submissionService: CoronaTestService
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var testResultDataCollector: TestResultDataCollector

    private val nowUTC = Instant.parse("2021-03-15T05:45:00.000Z")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns nowUTC

        submissionService.apply {
            coEvery { asyncRequestTestResult(any()) } returns PCR_OR_RAT_PENDING
            coEvery { asyncRegisterDeviceViaGUID(any()) } returns CoronaTestService.RegistrationData(
                registrationToken = "regtoken-qr",
                testResult = PCR_OR_RAT_PENDING,
            )
            coEvery { asyncRegisterDeviceViaTAN(any()) } returns CoronaTestService.RegistrationData(
                registrationToken = "regtoken-tan",
                testResult = PCR_OR_RAT_PENDING,
            )
        }

        analyticsKeySubmissionCollector.apply {
            coEvery { reportRegisteredWithTeleTAN() } just Runs
            coEvery { reset() } just Runs
            coEvery { reportPositiveTestResultReceived() } just Runs
            coEvery { reportTestRegistered() } just Runs
        }
        testResultDataCollector.apply {
            coEvery { updatePendingTestResultReceivedTime(any()) } just Runs
            coEvery { saveTestResultAnalyticsSettings(any()) } just Runs
        }
    }

    fun createInstance() = PCRProcessor(
        timeStamper = timeStamper,
        submissionService = submissionService,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        testResultDataCollector = testResultDataCollector
    )

    @Test
    fun `if we receive a pending result 60 days after registration, we map to REDEEMED`() = runBlockingTest {
        val instance = createInstance()

        val pcrTest = PCRCoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = nowUTC,
            registrationToken = "regtoken",
            testResult = PCR_POSITIVE
        )

        instance.pollServer(pcrTest).testResult shouldBe PCR_OR_RAT_PENDING

        val past60DaysTest = pcrTest.copy(
            registeredAt = nowUTC.minus(Duration.standardDays(21))
        )

        instance.pollServer(past60DaysTest).testResult shouldBe PCR_REDEEMED
    }

    @Test
    fun `registering a new test maps invalid results to INVALID state`() = runBlockingTest {
        var registrationData = CoronaTestService.RegistrationData(
            registrationToken = "regtoken",
            testResult = PCR_OR_RAT_PENDING,
        )
        coEvery { submissionService.asyncRegisterDeviceViaGUID(any()) } answers { registrationData }

        val instance = createInstance()

        val request = CoronaTestQRCode.PCR(qrCodeGUID = "guid")

        values().forEach {
            registrationData = registrationData.copy(testResult = it)
            when (it) {
                PCR_OR_RAT_PENDING,
                PCR_NEGATIVE,
                PCR_POSITIVE,
                PCR_INVALID,
                PCR_REDEEMED -> instance.create(request).testResult shouldBe it

                RAT_PENDING,
                RAT_NEGATIVE,
                RAT_POSITIVE,
                RAT_INVALID,
                RAT_REDEEMED ->
                    instance.create(request).testResult shouldBe PCR_INVALID
            }
        }
    }

    @Test
    fun `polling maps invalid results to INVALID state`() = runBlockingTest {
        var pollResult: CoronaTestResult = PCR_OR_RAT_PENDING
        coEvery { submissionService.asyncRequestTestResult(any()) } answers { pollResult }

        val instance = createInstance()

        val pcrTest = PCRCoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = nowUTC,
            registrationToken = "regtoken",
            testResult = PCR_POSITIVE
        )

        values().forEach {
            pollResult = it
            when (it) {
                PCR_OR_RAT_PENDING,
                PCR_NEGATIVE,
                PCR_POSITIVE,
                PCR_INVALID,
                PCR_REDEEMED -> {
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
    fun `registering a TAN test automatically consumes the notification flag`() = runBlockingTest {
        val instance = createInstance()

        instance.create(CoronaTestTAN.PCR(tan = "thisIsATan")).apply {
            isResultAvailableNotificationSent shouldBe true
        }

        instance.create(CoronaTestQRCode.PCR(qrCodeGUID = "thisIsAQRCodeGUID")).apply {
            isResultAvailableNotificationSent shouldBe false
        }
    }
}
