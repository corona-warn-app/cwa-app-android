package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.formatter.TestResult
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class TestResultDataCollectorTest : BaseTest() {

    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var testResultDonorSettings: TestResultDonorSettings
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var timeStamper: TimeStamper

    private lateinit var testResultDataCollector: TestResultDataCollector

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.parse("2021-03-02T09:57:11+01:00")
        every { testResultDonorSettings.clear() } just Runs
        testResultDataCollector = TestResultDataCollector(
            analyticsSettings,
            testResultDonorSettings,
            riskLevelStorage,
            timeStamper
        )
    }

    @Test
    fun `saveTestResultAnalyticsSettings does not save anything when no user consent`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
            testResultDataCollector.saveTestResultAnalyticsSettings(TestResult.POSITIVE)

            verify(exactly = 0) {
                testResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any())
            }
        }

    @Test
    fun `saveTestResultAnalyticsSettings saves data when user gave consent`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)

            val mockRiskLevelResult = mockk<EwRiskLevelResult>().apply {
                every { calculatedAt } returns Instant.now()
                every { wasSuccessfullyCalculated } returns true
            }
            every { riskLevelStorage.latestAndLastSuccessfulEwRiskLevelResult } returns flowOf(listOf(mockRiskLevelResult))
            every { testResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any()) } just Runs
            testResultDataCollector.saveTestResultAnalyticsSettings(TestResult.POSITIVE)

            verify(exactly = 1) {
                testResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any())
            }
        }

    @Test
    fun `saveTestResultAnalyticsSettings does not save data when TestResult is INVALID`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
            testResultDataCollector.saveTestResultAnalyticsSettings(TestResult.INVALID)

            verify {
                analyticsSettings.analyticsEnabled wasNot Called
            }
        }

    @Test
    fun `saveTestResultAnalyticsSettings does not save data when TestResult is REDEEMED`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
            testResultDataCollector.saveTestResultAnalyticsSettings(TestResult.REDEEMED)

            verify {
                analyticsSettings.analyticsEnabled wasNot Called
            }
        }

    @Test
    fun `updatePendingTestResultReceivedTime doesn't update when TestResult isn't POS or NEG`() =
        runBlockingTest {
            for (testResult in listOf(TestResult.REDEEMED, TestResult.INVALID, TestResult.PENDING)) {
                every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
                every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
                every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(TestResult.PENDING)
                testResultDataCollector.updatePendingTestResultReceivedTime(testResult)

                verify {
                    analyticsSettings.analyticsEnabled
                    testResultDonorSettings.testScannedAfterConsent
                    testResultDonorSettings.testResultAtRegistration
                    testResultDonorSettings.finalTestResultReceivedAt wasNot Called
                    testResultDonorSettings.testResultAtRegistration wasNot Called
                }
            }
        }

    @Test
    fun `updatePendingTestResultReceivedTime doesn't update when Test is not scanned after consent`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
            every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(false)
            every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(TestResult.PENDING)
            testResultDataCollector.updatePendingTestResultReceivedTime(TestResult.NEGATIVE)

            verify {
                analyticsSettings.analyticsEnabled
                testResultDonorSettings.testScannedAfterConsent
                testResultDonorSettings.testResultAtRegistration wasNot Called
                testResultDonorSettings.finalTestResultReceivedAt wasNot Called
                testResultDonorSettings.testResultAtRegistration wasNot Called
            }
        }

    @Test
    fun `updatePendingTestResultReceivedTime update when TestResult is POS or NEG`() =
        runBlockingTest {
            for (testResult in listOf(TestResult.NEGATIVE, TestResult.POSITIVE)) {
                every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
                every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
                every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(TestResult.PENDING)
                every { testResultDonorSettings.finalTestResultReceivedAt } returns mockFlowPreference(Instant.EPOCH)
                testResultDataCollector.updatePendingTestResultReceivedTime(testResult)

                verify {
                    analyticsSettings.analyticsEnabled
                    testResultDonorSettings.testScannedAfterConsent
                    testResultDonorSettings.testResultAtRegistration
                    testResultDonorSettings.finalTestResultReceivedAt
                    testResultDonorSettings.testResultAtRegistration
                }
            }
        }

    @Test
    fun `clear is clearing saved data`() {
        testResultDataCollector.clear()
        verify { testResultDonorSettings.clear() }
    }
}
