package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.common.AnalyticsExposureWindow
import de.rki.coronawarnapp.datadonation.analytics.common.AnalyticsScanInstance
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AnalyticsRATestResultDonorTest : BaseTest() {
    @MockK lateinit var testResultSettings: AnalyticsRATestResultSettings
    @MockK lateinit var timeStamper: TimeStamper
    private lateinit var testResultDonor: AnalyticsTestResultDonor
    private val baseTime = Instant.ofEpochMilli(101010101)
    @MockK lateinit var analyticsExposureWindow: AnalyticsExposureWindow
    @MockK lateinit var analyticsScanInstance: AnalyticsScanInstance

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
            every { testRegisteredAt } returns flowOf(baseTime)
            every { ewRiskLevelAtTestRegistration } returns flowOf(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            every { ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns flowOf(1)
            every { ewHoursSinceHighRiskWarningAtTestRegistration } returns flowOf(1)
            every { ptRiskLevelAtTestRegistration } returns flowOf(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            every { ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns flowOf(1)
            every { ptHoursSinceHighRiskWarningAtTestRegistration } returns flowOf(1)
            every { exposureWindowsAtTestRegistration } returns
                flowOf(listOf(analyticsExposureWindow, analyticsExposureWindow))
            every { exposureWindowsUntilTestResult } returns flowOf(listOf(analyticsExposureWindow))
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
    fun `No donation when timestamp at registration is missing`() = runTest {
        every { testResultSettings.testRegisteredAt } returns flowOf(null)
        testResultDonor.beginDonation(TestRequest) shouldBe AnalyticsTestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is INVALID`() = runTest {
        every { testResultSettings.testResult } returns flowOf(CoronaTestResult.RAT_INVALID)
        every { testResultSettings.finalTestResultReceivedAt } returns flowOf(null)
        testResultDonor.beginDonation(TestRequest) shouldBe AnalyticsTestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is REDEEMED`() = runTest {
        every { testResultSettings.testResult } returns flowOf(CoronaTestResult.RAT_REDEEMED)
        every { testResultSettings.finalTestResultReceivedAt } returns flowOf(null)
        testResultDonor.beginDonation(TestRequest) shouldBe AnalyticsTestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is PENDING and hours isn't greater or equal to config hours`() {
        every { testResultSettings.finalTestResultReceivedAt } returns flowOf(null)
        runTest {
            every { testResultSettings.testResult } returns
                flowOf(CoronaTestResult.PCR_OR_RAT_PENDING)

            testResultDonor.beginDonation(TestRequest) shouldBe
                AnalyticsTestResultDonor.TestResultMetadataNoContribution
        }
    }

    @Test
    fun `Donation is collected when test result is PENDING and hours is greater or equal to config hours`() {
        runTest {
            every { testResultSettings.testResult } returns
                flowOf(CoronaTestResult.PCR_OR_RAT_PENDING)
            val timeDayBefore = baseTime.minus(Duration.ofDays(1))
            every { testResultSettings.testRegisteredAt } returns flowOf(timeDayBefore)
            every { testResultSettings.finalTestResultReceivedAt } returns flowOf(null)

            val donation =
                testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                ptRiskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_RAT_PENDING
                hoursSinceTestRegistration shouldBe 24
                hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
                exposureWindowsAtTestRegistrationCount shouldBe 2
                exposureWindowsUntilTestResultCount shouldBe 1
            }
        }
    }

    @Test
    fun `Donation is collected when test result is POSITIVE`() {
        runTest {
            every { testResultSettings.testResult } returns
                flowOf(CoronaTestResult.RAT_POSITIVE)
            every { testResultSettings.finalTestResultReceivedAt } returns flowOf(baseTime)

            val donation =
                testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_RAT_POSITIVE
                hoursSinceTestRegistration shouldBe 0
                hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
                exposureWindowsAtTestRegistrationCount shouldBe 2
                exposureWindowsUntilTestResultCount shouldBe 1
            }
        }
    }

    @Test
    fun `Donation is collected when test result is NEGATIVE`() {
        runTest {
            every { testResultSettings.testResult } returns
                flowOf(CoronaTestResult.RAT_NEGATIVE)
            every { testResultSettings.finalTestResultReceivedAt } returns flowOf(baseTime)

            val donation =
                testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_RAT_NEGATIVE
                hoursSinceTestRegistration shouldBe 0
                hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
                exposureWindowsAtTestRegistrationCount shouldBe 2
                exposureWindowsUntilTestResultCount shouldBe 1
            }
        }
    }

    @Test
    fun `Scenario 1 LowRisk`() = runTest {
        with(testResultSettings) {
            every { testResult } returns flowOf(CoronaTestResult.RAT_NEGATIVE)
            every { finalTestResultReceivedAt } returns flowOf(
                Instant.parse("2021-03-20T20:00:00Z")
            )
            every { ewRiskLevelAtTestRegistration } returns flowOf(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            every { testRegisteredAt } returns flowOf(
                Instant.parse("2021-03-20T00:00:00Z")
            )
        }
        every { timeStamper.nowUTC } returns Instant.parse("2021-03-20T00:00:00Z")

        val donation =
            testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
        with(donation.testResultMetadata) {
            testResult shouldBe PpaData.PPATestResult.TEST_RESULT_RAT_NEGATIVE
            hoursSinceTestRegistration shouldBe 20
            riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
            hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
            exposureWindowsAtTestRegistrationCount shouldBe 2
            exposureWindowsUntilTestResultCount shouldBe 1
        }
    }

    @Test
    fun `Scenario 2 HighRisk`() = runTest {
        with(testResultSettings) {
            every { testResult } returns flowOf(CoronaTestResult.RAT_POSITIVE)
            every { finalTestResultReceivedAt } returns flowOf(
                Instant.parse("2021-03-20T20:00:00Z")
            )
            every { testRegisteredAt } returns flowOf(
                Instant.parse("2021-03-20T00:00:00Z")
            )
            every { ewRiskLevelAtTestRegistration } returns flowOf(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
        }

        every { timeStamper.nowUTC } returns Instant.parse("2021-03-20T00:00:00Z")

        val donation =
            testResultDonor.beginDonation(TestRequest) as AnalyticsTestResultDonor.TestResultMetadataContribution
        with(donation.testResultMetadata) {
            testResult shouldBe PpaData.PPATestResult.TEST_RESULT_RAT_POSITIVE
            hoursSinceTestRegistration shouldBe 20 // hours
            riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH
            hoursSinceHighRiskWarningAtTestRegistration shouldBe 1
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 1
            exposureWindowsAtTestRegistrationCount shouldBe 2
            exposureWindowsUntilTestResultCount shouldBe 1
        }
    }

    @Test
    fun deleteData() = runTest {
        coEvery { testResultSettings.clear() } just Runs

        testResultDonor.deleteData()

        coVerify {
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
