package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import android.os.SystemClock
import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.formatter.TestResult
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.preferences.mockFlowPreference

class TestResultDonorTest {
    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var riskLevelSettings: RiskLevelSettings
    @MockK lateinit var riskLevelStorage: RiskLevelStorage

    private lateinit var testResultDonor: TestResultDonor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, true)
        mockkObject(LocalData)

        coEvery { appConfigProvider.getAppConfig() } returns
            mockk<ConfigData>().apply {
                every { analytics } returns
                    mockk<AnalyticsConfig>().apply {
                        every { hoursSinceTestRegistrationToSubmitTestResultMetadata } returns 50
                    }
            }

        every { riskLevelSettings.lastChangeCheckedRiskLevelTimestamp } returns Instant.now()
        every { analyticsSettings.riskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)

        testResultDonor = TestResultDonor(
            analyticsSettings,
            appConfigProvider,
            riskLevelSettings,
            riskLevelStorage,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `No donation when user did not allow consent`() = runBlockingTest {
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(false)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when timestamp at registration is missing`() = runBlockingTest {
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { LocalData.initialTestResultReceivedTimestamp() } returns null
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is INVALID`() = runBlockingTest {
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { LocalData.initialTestResultReceivedTimestamp() } returns SystemClock.currentThreadTimeMillis()
        every { analyticsSettings.testResultAtRegistration } returns mockFlowPreference(TestResult.INVALID)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is REDEEMED`() = runBlockingTest {
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { LocalData.initialTestResultReceivedTimestamp() } returns SystemClock.currentThreadTimeMillis()
        every { analyticsSettings.testResultAtRegistration } returns mockFlowPreference(TestResult.REDEEMED)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun deleteData() = runBlockingTest {
        analyticsSettings.apply {
            every { testScannedAfterConsent } returns mockFlowPreference(false)
            every { riskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            every { finalTestResultReceivedAt } returns mockFlowPreference(Instant.EPOCH)
            every { testResultAtRegistration } returns mockFlowPreference(TestResult.INVALID)
        }

        testResultDonor.deleteData()

        verify {
            analyticsSettings.testScannedAfterConsent
            analyticsSettings.riskLevelAtTestRegistration
            analyticsSettings.finalTestResultReceivedAt
            analyticsSettings.testResultAtRegistration

            analyticsSettings.analyticsEnabled wasNot Called
        }
    }

    object TestRequest : DonorModule.Request
}
