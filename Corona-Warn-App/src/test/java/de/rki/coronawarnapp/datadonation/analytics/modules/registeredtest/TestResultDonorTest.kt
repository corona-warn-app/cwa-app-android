package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class TestResultDonorTest : BaseTest() {
    @MockK lateinit var testResultDonorSettings: TestResultDonorSettings
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var coronaTestRepository: CoronaTestRepository

    private lateinit var testResultDonor: TestResultDonor

    private val baseTime = Instant.ofEpochMilli(101010101)

    private val coronaTests: MutableStateFlow<Set<CoronaTest>> = MutableStateFlow(
        setOf(
            mockk<PCRCoronaTest>().apply {
                every { registeredAt } returns baseTime
                every { testResultReceivedAt } returns baseTime
                every { type } returns CoronaTest.Type.PCR
            }
        )
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, true)
        with(testResultDonorSettings) {
            every { mostRecentDateWithHighOrLowRiskLevel } returns mockFlowPreference(baseTime)
            every { riskLevelTurnedRedTime } returns mockFlowPreference(baseTime)
            every { riskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
        }
        every { timeStamper.nowUTC } returns baseTime
        every { coronaTestRepository.coronaTests } returns coronaTests

        testResultDonor = TestResultDonor(
            testResultDonorSettings,
            timeStamper,
            coronaTestRepository = coronaTestRepository
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `No donation when user did not allow consent`() = runBlockingTest {
        every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(false)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when timestamp at registration is missing`() = runBlockingTest {
        every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        coronaTests.value = emptySet()
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is INVALID`() = runBlockingTest {
        every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_INVALID)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is REDEEMED`() = runBlockingTest {
        every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_REDEEMED)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is PENDING and hours isn't greater or equal to config hours`() {
        runBlockingTest {
            every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
            every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_OR_RAT_PENDING)

            testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
        }
    }

    @Test
    fun `Donation is collected when test result is PENDING and hours is greater or equal to config hours`() {
        runBlockingTest {
            every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
            every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_OR_RAT_PENDING)

            val timeDayBefore = baseTime.minus(Duration.standardDays(1))
            coronaTests.value = setOf(
                mockk<PCRCoronaTest>().apply {
                    every { registeredAt } returns timeDayBefore
                    every { testResultReceivedAt } returns baseTime
                    every { type } returns CoronaTest.Type.PCR
                }
            )
            every { testResultDonorSettings.mostRecentDateWithHighOrLowRiskLevel } returns mockFlowPreference(
                timeDayBefore
            )

            val donation = testResultDonor.beginDonation(TestRequest)
            donation.shouldBeInstanceOf<TestResultDonor.TestResultMetadataContribution>()
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_PENDING
                hoursSinceTestRegistration shouldBe 24
                hoursSinceHighRiskWarningAtTestRegistration shouldBe -1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 0
            }
        }
    }

    @Test
    fun `Donation is collected when test result is POSITIVE`() {
        runBlockingTest {
            every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
            every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_POSITIVE)
            every { testResultDonorSettings.finalTestResultReceivedAt } returns mockFlowPreference(baseTime)

            val donation = testResultDonor.beginDonation(TestRequest)
            donation.shouldBeInstanceOf<TestResultDonor.TestResultMetadataContribution>()
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_POSITIVE
                hoursSinceTestRegistration shouldBe 0
                hoursSinceHighRiskWarningAtTestRegistration shouldBe -1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 0
            }
        }
    }

    @Test
    fun `No donation when test is POSITIVE and HighRisk but riskLevelTurnedRedTime is missing`() =
        runBlockingTest {
            with(testResultDonorSettings) {
                every { testScannedAfterConsent } returns mockFlowPreference(true)
                every { testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_POSITIVE)
                every { finalTestResultReceivedAt } returns mockFlowPreference(baseTime)
                every { riskLevelTurnedRedTime } returns mockFlowPreference(null)
                every { riskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            }
            testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
        }

    @Test
    fun `No donation when test is NEGATIVE and HighRisk but riskLevelTurnedRedTime is missing`() =
        runBlockingTest {
            with(testResultDonorSettings) {
                every { testScannedAfterConsent } returns mockFlowPreference(true)
                every { testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_NEGATIVE)
                every { finalTestResultReceivedAt } returns mockFlowPreference(baseTime)
                every { riskLevelTurnedRedTime } returns mockFlowPreference(null)
                every { riskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            }
            testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
        }

    @Test
    fun `Donation when test is POSITIVE and HighRisk but mostRecentDateWithHighOrLowRiskLevel is missing`() =
        runBlockingTest {
            with(testResultDonorSettings) {
                every { testScannedAfterConsent } returns mockFlowPreference(true)
                every { testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_POSITIVE)
                every { finalTestResultReceivedAt } returns mockFlowPreference(baseTime)
                every { riskLevelTurnedRedTime } returns mockFlowPreference(baseTime)
                every { mostRecentDateWithHighOrLowRiskLevel } returns mockFlowPreference(null)
                every { riskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            }

            val donation = testResultDonor.beginDonation(TestRequest)
            donation.shouldBeInstanceOf<TestResultDonor.TestResultMetadataContribution>()
            donation.testResultMetadata.apply {
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe -1
            }
        }

    @Test
    fun `Donation when test is NEGATIVE and HighRisk but mostRecentDateWithHighOrLowRiskLevel is missing`() =
        runBlockingTest {
            with(testResultDonorSettings) {
                every { testScannedAfterConsent } returns mockFlowPreference(true)
                every { testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_NEGATIVE)
                every { finalTestResultReceivedAt } returns mockFlowPreference(baseTime)
                every { riskLevelTurnedRedTime } returns mockFlowPreference(baseTime)
                every { mostRecentDateWithHighOrLowRiskLevel } returns mockFlowPreference(null)
                every { riskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            }
            val donation = testResultDonor.beginDonation(TestRequest)
            donation.shouldBeInstanceOf<TestResultDonor.TestResultMetadataContribution>()
            donation.testResultMetadata.apply {
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe -1
            }
        }

    @Test
    fun `Donation when test is  POSITIVE and LowRisk but mostRecentDateWithHighOrLowRiskLevel is missing`() =
        runBlockingTest {
            with(testResultDonorSettings) {
                every { testScannedAfterConsent } returns mockFlowPreference(true)
                every { testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_POSITIVE)
                every { finalTestResultReceivedAt } returns mockFlowPreference(baseTime)
                every { riskLevelTurnedRedTime } returns mockFlowPreference(null)
                every { mostRecentDateWithHighOrLowRiskLevel } returns mockFlowPreference(null)
            }

            val donation = testResultDonor.beginDonation(TestRequest)
            donation.shouldBeInstanceOf<TestResultDonor.TestResultMetadataContribution>()
            donation.testResultMetadata.apply {
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe -1
            }
        }

    @Test
    fun `Donation when test is NEGATIVE and LowRisk but mostRecentDateWithHighOrLowRiskLevel is missing`() =
        runBlockingTest {
            with(testResultDonorSettings) {
                every { testScannedAfterConsent } returns mockFlowPreference(true)
                every { testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_NEGATIVE)
                every { finalTestResultReceivedAt } returns mockFlowPreference(baseTime)
                every { riskLevelTurnedRedTime } returns mockFlowPreference(null)
                every { mostRecentDateWithHighOrLowRiskLevel } returns mockFlowPreference(null)
            }
            val donation = testResultDonor.beginDonation(TestRequest)
            donation.shouldBeInstanceOf<TestResultDonor.TestResultMetadataContribution>()
            donation.testResultMetadata.apply {
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe -1
            }
        }

    @Test
    fun `Donation is collected when test result is NEGATIVE`() {
        runBlockingTest {
            every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
            every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_NEGATIVE)
            every { testResultDonorSettings.finalTestResultReceivedAt } returns mockFlowPreference(baseTime)

            val donation = testResultDonor.beginDonation(TestRequest)
            donation.shouldBeInstanceOf<TestResultDonor.TestResultMetadataContribution>()
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_NEGATIVE
                hoursSinceTestRegistration shouldBe 0
                hoursSinceHighRiskWarningAtTestRegistration shouldBe -1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 0
            }
        }
    }

    @Test
    fun `Scenario 1 LowRisk`() = runBlockingTest {
        with(testResultDonorSettings) {
            every { testScannedAfterConsent } returns mockFlowPreference(true)
            every { testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_NEGATIVE)
            every { finalTestResultReceivedAt } returns mockFlowPreference(
                Instant.parse("2021-03-20T20:00:00Z")
            )
            every { riskLevelTurnedRedTime } returns mockFlowPreference(null) // No High risk
            every { mostRecentDateWithHighOrLowRiskLevel } returns
                mockFlowPreference(Instant.parse("2021-03-18T00:00:00Z"))
            every { riskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
        }
        every { timeStamper.nowUTC } returns Instant.parse("2021-03-20T00:00:00Z")
        coronaTests.value = setOf(
            mockk<PCRCoronaTest>().apply {
                every { testResultReceivedAt } returns baseTime
                every { registeredAt } returns Instant.parse("2021-03-20T00:00:00Z")
                every { type } returns CoronaTest.Type.PCR
            }
        )

        val donation = testResultDonor.beginDonation(TestRequest)
        donation.shouldBeInstanceOf<TestResultDonor.TestResultMetadataContribution>()
        with(donation.testResultMetadata) {
            testResult shouldBe PpaData.PPATestResult.TEST_RESULT_NEGATIVE
            hoursSinceTestRegistration shouldBe 20 // hours
            riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
            hoursSinceHighRiskWarningAtTestRegistration shouldBe -1 // expected for low risk
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 2 // days
        }
    }

    @Test
    fun `Scenario 2 HighRisk`() = runBlockingTest {
        with(testResultDonorSettings) {
            every { testScannedAfterConsent } returns mockFlowPreference(true)
            every { testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_POSITIVE)
            every { finalTestResultReceivedAt } returns mockFlowPreference(
                Instant.parse("2021-03-20T20:00:00Z")
            )
            every { riskLevelTurnedRedTime } returns mockFlowPreference(Instant.parse("2021-03-01T00:00:00Z"))
            every { mostRecentDateWithHighOrLowRiskLevel } returns
                mockFlowPreference(Instant.parse("2021-03-18T00:00:00Z"))
            every { riskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        }

        every { timeStamper.nowUTC } returns Instant.parse("2021-03-20T00:00:00Z")
        coronaTests.value = setOf(
            mockk<PCRCoronaTest>().apply {
                every { testResultReceivedAt } returns baseTime
                every { registeredAt } returns Instant.parse("2021-03-20T00:00:00Z")
                every { type } returns CoronaTest.Type.PCR
            }
        )

        val donation = testResultDonor.beginDonation(TestRequest)
        donation.shouldBeInstanceOf<TestResultDonor.TestResultMetadataContribution>()
        with(donation.testResultMetadata) {
            testResult shouldBe PpaData.PPATestResult.TEST_RESULT_POSITIVE
            hoursSinceTestRegistration shouldBe 20 // hours
            riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
            hoursSinceHighRiskWarningAtTestRegistration shouldBe 456 // 19 days in hours
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 2 // days
        }
    }

    @Test
    fun deleteData() = runBlockingTest {
        every { testResultDonorSettings.clear() } just Runs

        testResultDonor.deleteData()

        verify {
            testResultDonorSettings.clear()
        }
    }

    object TestRequest : DonorModule.Request {
        override val currentConfig: ConfigData
            get() = mockk<ConfigData>().apply {
                every { analytics } returns
                    mockk<AnalyticsConfig>().apply {
                        every { hoursSinceTestRegistrationToSubmitTestResultMetadata } returns 20
                    }
            }
    }
}
