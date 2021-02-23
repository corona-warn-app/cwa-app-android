package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.formatter.TestResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

import testhelpers.preferences.mockFlowPreference
import java.util.concurrent.TimeUnit

class TestResultDonorTest : BaseTest() {
    @MockK lateinit var testResultDonorSettings: TestResultDonorSettings
    @MockK lateinit var riskLevelSettings: RiskLevelSettings
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var timeStamper: TimeStamper

    private lateinit var testResultDonor: TestResultDonor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, true)
        mockkObject(LocalData)
        every { timeStamper.nowUTC } returns Instant.now()
        every { riskLevelSettings.lastChangeCheckedRiskLevelTimestamp } returns Instant.now()
        every { testResultDonorSettings.riskLevelAtTestRegistration } returns
            mockFlowPreference(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
        every { LocalData.initialTestResultReceivedTimestamp() } returns System.currentTimeMillis()

        testResultDonor = TestResultDonor(
            testResultDonorSettings,
            riskLevelSettings,
            riskLevelStorage,
            timeStamper,
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
        every { LocalData.initialTestResultReceivedTimestamp() } returns null
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is INVALID`() = runBlockingTest {
        every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(TestResult.INVALID)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is REDEEMED`() = runBlockingTest {
        every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
        every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(TestResult.REDEEMED)
        testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
    }

    @Test
    fun `No donation when test result is PENDING and hours isn't greater or equal to config hours`() {
        runBlockingTest {
            every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
            every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(TestResult.PENDING)

            testResultDonor.beginDonation(TestRequest) shouldBe TestResultDonor.TestResultMetadataNoContribution
        }
    }

    @Test
    fun `Donation is collected when test result is PENDING and hours is greater or equal to config hours`() {
        runBlockingTest {
            every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
            every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(TestResult.PENDING)

            val timeDayBefore = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
            every { LocalData.initialTestResultReceivedTimestamp() } returns timeDayBefore
            every { riskLevelSettings.lastChangeCheckedRiskLevelTimestamp } returns Instant.ofEpochMilli(timeDayBefore)

            val donation = testResultDonor.beginDonation(TestRequest)
            donation.shouldBeInstanceOf<TestResultDonor.TestResultMetadataContribution>()
            with(donation.testResultMetadata) {
                riskLevelAtTestRegistration shouldBe PpaData.PPARiskLevel.RISK_LEVEL_LOW
                testResult shouldBe PpaData.PPATestResult.TEST_RESULT_PENDING
                hoursSinceTestRegistration shouldBe 23
                hoursSinceHighRiskWarningAtTestRegistration shouldBe -1
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 0
            }
        }
    }

    @Test
    fun `Donation is collected when test result is POSITIVE`() {
        runBlockingTest {
            every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
            every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(TestResult.POSITIVE)
            every { testResultDonorSettings.finalTestResultReceivedAt } returns mockFlowPreference(Instant.now())

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
    fun `Donation is collected when test result is NEGATIVE`() {
        runBlockingTest {
            every { testResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
            every { testResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(TestResult.NEGATIVE)
            every { testResultDonorSettings.finalTestResultReceivedAt } returns mockFlowPreference(Instant.now())

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
