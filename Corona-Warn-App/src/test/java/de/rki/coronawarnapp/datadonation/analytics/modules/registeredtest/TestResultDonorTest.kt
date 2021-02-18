package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import android.os.SystemClock
import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.ui.homecards.FetchingResult
import de.rki.coronawarnapp.submission.ui.homecards.NoTest
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionStateProvider
import de.rki.coronawarnapp.submission.ui.homecards.TestError
import de.rki.coronawarnapp.submission.ui.homecards.TestInvalid
import de.rki.coronawarnapp.submission.ui.homecards.TestResultReady
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.preferences.mockFlowPreference

class TestResultDonorTest {

    @MockK lateinit var submissionStateProvider: SubmissionStateProvider
    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var riskLevelSettings: RiskLevelSettings

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
            submissionStateProvider,
            analyticsSettings,
            appConfigProvider,
            riskLevelSettings
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
    fun `No donation when test result is invalid`() = runBlockingTest {
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { LocalData.initialTestResultReceivedTimestamp() } returns SystemClock.currentThreadTimeMillis()
        every { submissionStateProvider.state } returns flowOf(TestInvalid)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is TestError`() = runBlockingTest {
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { LocalData.initialTestResultReceivedTimestamp() } returns SystemClock.currentThreadTimeMillis()
        every { submissionStateProvider.state } returns flowOf(TestError)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is TestResultReady`() = runBlockingTest {
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { LocalData.initialTestResultReceivedTimestamp() } returns SystemClock.currentThreadTimeMillis()
        every { submissionStateProvider.state } returns flowOf(TestResultReady)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is FetchingResult`() = runBlockingTest {
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { LocalData.initialTestResultReceivedTimestamp() } returns SystemClock.currentThreadTimeMillis()
        every { submissionStateProvider.state } returns flowOf(FetchingResult)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is NoTest`() = runBlockingTest {
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { LocalData.initialTestResultReceivedTimestamp() } returns SystemClock.currentThreadTimeMillis()
        every { submissionStateProvider.state } returns flowOf(NoTest)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun deleteData() = runBlockingTest {
        analyticsSettings.apply {
            every { testScannedAfterConsent } returns mockFlowPreference(false)
            every { riskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            every { finalTestResultReceivedAt } returns mockFlowPreference(Instant.EPOCH)
        }

        testResultDonor.deleteData()

        verify {
            analyticsSettings.testScannedAfterConsent
            analyticsSettings.riskLevelAtTestRegistration
            analyticsSettings.finalTestResultReceivedAt

            analyticsSettings.analyticsEnabled wasNot Called
        }
    }

    object TestRequest : DonorModule.Request
}
