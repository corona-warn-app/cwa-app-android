package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindow
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsScanInstance
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
import kotlinx.coroutines.test.runTest
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class AnalyticsPCRTestResultDonorTest : BaseTest() {
    @MockK lateinit var testResultSettings: AnalyticsPCRTestResultSettings
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var analyticsExposureWindow: AnalyticsExposureWindow
    @MockK lateinit var analyticsScanInstance: AnalyticsScanInstance

    private lateinit var testResultDonor: AnalyticsTestResultDonor

    private val baseTime = Instant.ofEpochMilli(101010101)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, true)

        with(analyticsScanInstance) {
            every { minAttenuation } returns 1
            every { typicalAttenuation } returns 2
            every { secondsSinceLastScan } returns 3
        }

        with(analyticsExposureWindow) {
            every { analyticsScanInstances } returns listOf(analyticsScanInstance)
            every { calibrationConfidence } returns 4
            every { dateMillis } returns 1000L
            every { infectiousness } returns 5
            every { reportType } returns 6
            every { normalizedTime } returns 1.1
            every { transmissionRiskLevel } returns 7
        }

        with(testResultSettings) {
            every { testRegisteredAt } returns mockFlowPreference(baseTime)
            every { ewRiskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            every { ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns mockFlowPreference(1)
            every { ewHoursSinceHighRiskWarningAtTestRegistration } returns mockFlowPreference(1)
            every { ptRiskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            every { ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns mockFlowPreference(1)
            every { ptHoursSinceHighRiskWarningAtTestRegistration } returns mockFlowPreference(1)
            every { exposureWindowsAtTestRegistration } returns mockFlowPreference(listOf(analyticsExposureWindow))
            every { exposureWindowsUntilTestResult } returns
                mockFlowPreference(listOf(analyticsExposureWindow, analyticsExposureWindow))
        }
        every { timeStamper.nowUTC } returns baseTime

        testResultDonor = AnalyticsPCRTestResultDonor(
            testResultSettings,
            timeStamper
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `No donation when timestamp at registration is missing`() = runTest {
        every { testResultSettings.testRegisteredAt } returns mockFlowPreference(null)
        testResultDonor.beginDonation(TestRequest) shouldBe AnalyticsTestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is INVALID`() = runTest {
        every { testResultSettings.testResult } returns mockFlowPreference(CoronaTestResult.PCR_INVALID)
        every { testResultSettings.finalTestResultReceivedAt } returns mockFlowPreference(null)
        testResultDonor.beginDonation(TestRequest) shouldBe AnalyticsTestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is REDEEMED`() = runTest {
        every { testResultSettings.testResult } returns mockFlowPreference(CoronaTestResult.PCR_OR_RAT_REDEEMED)
        every { testResultSettings.finalTestResultReceivedAt } returns mockFlowPreference(null)
        testResultDonor.beginDonation(TestRequest) shouldBe AnalyticsTestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is PENDING and hours isn't greater or equal to config hours`() {
        runTest {
            every { testResultSettings.testResult } returns
                mockFlowPreference(CoronaTestResult.PCR_OR_RAT_PENDING)
            every { testResultSettings.finalTestResultReceivedAt } returns mockFlowPreference(null)

            testResultDonor.beginDonation(TestRequest) shouldBe
                AnalyticsTestResultDonor.TestResultMetadataNoContribution
        }
    }

    @Test
    fun `Donation is collected when test result is PENDING and hours is greater or equal to config hours`() {
        runTest {
            every { testResultSettings.testResult } returns
                mockFlowPreference(CoronaTestResult.PCR_OR_RAT_PENDING)
            val timeDayBefore = baseTime.minus(Duration.ofDays(1))
            every { testResultSettings.testRegisteredAt } returns mockFlowPreference(timeDayBefore)
            every { testResultSettings.finalTestResultReceivedAt } returns mockFlowPreference(null)

            val donation =
                testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                ptRiskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_PENDING
                hoursSinceTestRegistration shouldBe 24
                hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
                exposureWindowsAtTestRegistrationCount shouldBe 1
                exposureWindowsUntilTestResultCount shouldBe 2
            }
        }
    }

    @Test
    fun `Donation is collected when test result is POSITIVE`() {
        runTest {
            every { testResultSettings.testResult } returns
                mockFlowPreference(CoronaTestResult.PCR_POSITIVE)
            every { testResultSettings.finalTestResultReceivedAt } returns
                mockFlowPreference(baseTime)

            val donation =
                testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_POSITIVE
                hoursSinceTestRegistration shouldBe 0
                hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
                exposureWindowsAtTestRegistrationCount shouldBe 1
                exposureWindowsUntilTestResultCount shouldBe 2
            }
        }
    }

    @Test
    fun `Donation is collected when test result is NEGATIVE`() {
        runTest {
            every { testResultSettings.testResult } returns
                mockFlowPreference(CoronaTestResult.PCR_NEGATIVE)
            every { testResultSettings.finalTestResultReceivedAt } returns
                mockFlowPreference(baseTime)

            val donation =
                testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_NEGATIVE
                hoursSinceTestRegistration shouldBe 0
                hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
                exposureWindowsAtTestRegistrationCount shouldBe 1
                exposureWindowsUntilTestResultCount shouldBe 2
            }
        }
    }

    @Test
    fun `Scenario 1 LowRisk`() = runTest {
        with(testResultSettings) {
            every { testResult } returns mockFlowPreference(CoronaTestResult.PCR_NEGATIVE)
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
            exposureWindowsAtTestRegistrationCount shouldBe 1
            exposureWindowsUntilTestResultCount shouldBe 2
        }
    }

    @Test
    fun `Scenario 2 HighRisk`() = runTest {
        with(testResultSettings) {
            every { testResult } returns mockFlowPreference(CoronaTestResult.PCR_POSITIVE)
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
            exposureWindowsAtTestRegistrationCount shouldBe 1
            exposureWindowsUntilTestResultCount shouldBe 2
        }
    }

    @Test
    fun deleteData() = runTest {
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
