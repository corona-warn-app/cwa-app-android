package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionStateProvider
import io.kotest.matchers.shouldBe
import io.mockk.Call
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import testhelpers.coroutines.runBlockingTest2
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
    fun deleteData() = runBlockingTest {
        analyticsSettings.apply {
            every { testScannedAfterConsent } returns mockFlowPreference(false)
            every { riskLevelAtTestRegistration } returns mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
            every { pendingResultReceivedAt } returns mockFlowPreference(Instant.EPOCH)
        }

        testResultDonor.deleteData()

        verify {
            analyticsSettings.testScannedAfterConsent
            analyticsSettings.riskLevelAtTestRegistration
            analyticsSettings.pendingResultReceivedAt

            analyticsSettings.analyticsEnabled wasNot Called
        }
    }

    object TestRequest : DonorModule.Request
}