package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
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

    private lateinit var testResultDataCollector: TestResultDataCollector

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        testResultDataCollector = TestResultDataCollector(
            analyticsSettings,
            testResultDonorSettings,
            riskLevelStorage
        )
    }

    @Test
    fun `saveTestResultAnalyticsSettings does not save anything when no user consent`() = runBlockingTest {
        every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
        testResultDataCollector.saveTestResultAnalyticsSettings(TestResult.POSITIVE)

        verify(exactly = 0) {
            testResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any())
        }
    }

    @Test
    fun `saveTestResultAnalyticsSettings saves data when user gave consent`() = runBlockingTest {
        every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)

        val mockRiskLevelResult = mockk<RiskLevelResult>().apply {
            every { calculatedAt } returns Instant.now()
            every { wasSuccessfullyCalculated } returns true
        }
        every { riskLevelStorage.latestAndLastSuccessful } returns flowOf(listOf(mockRiskLevelResult))
        every { testResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any()) } just Runs
        testResultDataCollector.saveTestResultAnalyticsSettings(TestResult.POSITIVE)

        verify(exactly = 1) {
            testResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any())
        }
    }

    @Test
    fun `saveTestResultAnalyticsSettings does not save data when TestResult is INVALID`() = runBlockingTest {
        every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
        testResultDataCollector.saveTestResultAnalyticsSettings(TestResult.INVALID)

        verify {
            analyticsSettings.analyticsEnabled wasNot Called
        }
    }

    @Test
    fun `saveTestResultAnalyticsSettings does not save data when TestResult is REDEEMED`() = runBlockingTest {
        every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
        testResultDataCollector.saveTestResultAnalyticsSettings(TestResult.REDEEMED)

        verify {
            analyticsSettings.analyticsEnabled wasNot Called
        }
    }
}
