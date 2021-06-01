package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class AnalyticsRATestResultDonorTest : BaseTest() {
    @MockK lateinit var testResultSettings: AnalyticsRATestResultSettings
    @MockK lateinit var timeStamper: TimeStamper

    private lateinit var testResultDonor: AnalyticsTestResultDonor

    private val baseTime = Instant.ofEpochMilli(101010101)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, true)
        with(testResultSettings) {
            every { testRegisteredAt } returns mockFlowPreference(baseTime)
            every { ewRiskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            every { ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns mockFlowPreference(1)
            every { ewHoursSinceHighRiskWarningAtTestRegistration } returns mockFlowPreference(1)
            every { ptRiskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            every { ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns mockFlowPreference(1)
            every { ptHoursSinceHighRiskWarningAtTestRegistration } returns mockFlowPreference(1)
        }
        every { timeStamper.nowUTC } returns baseTime

        testResultDonor = AnalyticsRATestResultDonor(
            testResultSettings,
            timeStamper
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `No donation when timestamp at registration is missing`() = runBlockingTest {
        every { testResultSettings.testRegisteredAt } returns mockFlowPreference(null)
        testResultDonor.beginDonation(TestRequest) shouldBe AnalyticsTestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is INVALID`() = runBlockingTest {
        every { testResultSettings.testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_INVALID)
        testResultDonor.beginDonation(TestRequest) shouldBe AnalyticsTestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is REDEEMED`() = runBlockingTest {
        every { testResultSettings.testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_REDEEMED)
        testResultDonor.beginDonation(TestRequest) shouldBe AnalyticsTestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is PENDING and hours isn't greater or equal to config hours`() {
        runBlockingTest {
            every { testResultSettings.testResultAtRegistration } returns
                mockFlowPreference(CoronaTestResult.PCR_OR_RAT_PENDING)

            testResultDonor.beginDonation(TestRequest) shouldBe
                AnalyticsTestResultDonor.TestResultMetadataNoContribution
        }
    }

    @Test
    fun `Donation is collected when test result is PENDING and hours is greater or equal to config hours`() {
        runBlockingTest {
            every { testResultSettings.testResultAtRegistration } returns
                mockFlowPreference(CoronaTestResult.PCR_OR_RAT_PENDING)
            val timeDayBefore = baseTime.minus(Duration.standardDays(1))
            every { testResultSettings.testRegisteredAt } returns mockFlowPreference(timeDayBefore)

            val donation =
                testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_PENDING
                hoursSinceTestRegistration shouldBe 24
                hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
            }
        }
    }

    @Test
    fun `Donation is collected when test result is POSITIVE`() {
        runBlockingTest {
            every { testResultSettings.testResultAtRegistration } returns
                mockFlowPreference(CoronaTestResult.PCR_POSITIVE)
            every { testResultSettings.finalTestResultReceivedAt } returns mockFlowPreference(baseTime)

            val donation =
                testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_POSITIVE
                hoursSinceTestRegistration shouldBe 0
                hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
            }
        }
    }

    @Test
    fun `Donation is collected when test result is NEGATIVE`() {
        runBlockingTest {
            every { testResultSettings.testResultAtRegistration } returns
                mockFlowPreference(CoronaTestResult.PCR_NEGATIVE)
            every { testResultSettings.finalTestResultReceivedAt } returns mockFlowPreference(baseTime)

            val donation =
                testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_NEGATIVE
                hoursSinceTestRegistration shouldBe 0
                hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
            }
        }
    }

    @Test
    fun `Scenario 1 LowRisk`() = runBlockingTest {
        with(testResultSettings) {
            every { testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_NEGATIVE)
            every { finalTestResultReceivedAt } returns mockFlowPreference(
                Instant.parse("2021-03-20T20:00:00Z")
            )
            every { ewRiskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            every { testRegisteredAt } returns mockFlowPreference(
                Instant.parse("2021-03-20T00:00:00Z")
            )
        }
        every { timeStamper.nowUTC } returns Instant.parse("2021-03-20T00:00:00Z")

        val donation =
            testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
        with(donation.testResultMetadata) {
            testResult shouldBe PpaData.PPATestResult.TEST_RESULT_NEGATIVE
            hoursSinceTestRegistration shouldBe 20
            riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
            hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
        }
    }

    @Test
    fun `Scenario 2 HighRisk`() = runBlockingTest {
        with(testResultSettings) {
            every { testResultAtRegistration } returns mockFlowPreference(CoronaTestResult.PCR_POSITIVE)
            every { finalTestResultReceivedAt } returns mockFlowPreference(
                Instant.parse("2021-03-20T20:00:00Z")
            )
            every { testRegisteredAt } returns mockFlowPreference(
                Instant.parse("2021-03-20T00:00:00Z")
            )
            every { ewRiskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        }

        every { timeStamper.nowUTC } returns Instant.parse("2021-03-20T00:00:00Z")

        val donation =
            testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
        with(donation.testResultMetadata) {
            testResult shouldBe PpaData.PPATestResult.TEST_RESULT_POSITIVE
            hoursSinceTestRegistration shouldBe 20 // hours
            riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
            hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
        }
    }

    @Test
    fun deleteData() = runBlockingTest {
        every { testResultSettings.clear() } just Runs

        testResultDonor.deleteData()

        verify {
            testResultSettings.clear()
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
