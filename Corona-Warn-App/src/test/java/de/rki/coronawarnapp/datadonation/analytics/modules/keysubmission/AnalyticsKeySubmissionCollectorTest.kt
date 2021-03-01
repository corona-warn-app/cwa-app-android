package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Hours
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class AnalyticsKeySubmissionCollectorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var analyticsKeySubmissionStorage: AnalyticsKeySubmissionStorage
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var riskLevelSettings: RiskLevelSettings
    @MockK lateinit var riskLevelResult: RiskLevelResult

    private val now = Instant.now()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns now
    }

    @Test
    fun testReportTestRegistered() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        every { riskLevelResult.riskState } returns RiskState.INCREASED_RISK
        coEvery {
            riskLevelStorage.latestAndLastSuccessful
        } returns flowOf(listOf(riskLevelResult))
        every { riskLevelSettings.lastChangeToHighRiskLevelTimestamp } returns now.minus(
            Hours.hours(2).toStandardDuration()
        )
        val testRegisteredAt = mockFlowPreference(now.millis)
        coEvery { analyticsKeySubmissionStorage.testRegisteredAt } returns testRegisteredAt
        every { riskLevelResult.wasSuccessfullyCalculated } returns true
        val riskLevelAtTestRegistration = mockFlowPreference(-1)
        every { analyticsKeySubmissionStorage.riskLevelAtTestRegistration } returns riskLevelAtTestRegistration
        val hoursSinceHighRiskWarningAtTestRegistration = mockFlowPreference(-1)
        every { analyticsKeySubmissionStorage.hoursSinceHighRiskWarningAtTestRegistration } returns hoursSinceHighRiskWarningAtTestRegistration
        runBlockingTest {
            val collector = createInstance()
            collector.reportTestRegistered()
            verify { testRegisteredAt.update(any()) }
            verify { riskLevelAtTestRegistration.update(any()) }
            verify { hoursSinceHighRiskWarningAtTestRegistration.update(any()) }
        }
    }

    @Test
    fun testReportSubmitted() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val submittedFlow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.submitted } returns submittedFlow
        val submittedAtFlow = mockFlowPreference(now.millis)
        every { analyticsKeySubmissionStorage.submittedAt } returns submittedAtFlow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmitted()
            verify { submittedFlow.update(any()) }
            verify { submittedAtFlow.update(any()) }
        }
    }

    @Test
    fun testReportSubmittedAfterCancel() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.submittedAfterCancel } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmittedAfterCancel()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun testReportSubmittedInBackground() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.submittedInBackground } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmittedInBackground()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun testReportSubmittedAfterSymptomFlow() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.submittedAfterSymptomFlow } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmittedAfterSymptomFlow()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun testReportPositiveTestResultReceived() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(now.millis)
        every { analyticsKeySubmissionStorage.testResultReceivedAt } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportPositiveTestResultReceived()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun testReportAdvancedConsentGiven() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.advancedConsentGiven } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportAdvancedConsentGiven()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun testReportConsentWithdrawn() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.advancedConsentGiven } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportConsentWithdrawn()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun testReportRegisteredWithTeleTAN() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.registeredWithTeleTAN } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportRegisteredWithTeleTAN()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun testReportLastSubmissionFlowScreen() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(0)
        every { analyticsKeySubmissionStorage.lastSubmissionFlowScreen } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportLastSubmissionFlowScreen(Screen.WARN_OTHERS)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun testNoCollectionIfDisabled() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns false
        runBlockingTest {
            val collector = createInstance()
            collector.reportTestRegistered()
            verify(exactly = 0) { analyticsKeySubmissionStorage.testRegisteredAt }
            verify(exactly = 0) { analyticsKeySubmissionStorage.riskLevelAtTestRegistration }
            verify(exactly = 0) { analyticsKeySubmissionStorage.hoursSinceHighRiskWarningAtTestRegistration }
            collector.reportSubmitted()
            verify(exactly = 0) { analyticsKeySubmissionStorage.submitted }
            collector.reportSubmittedInBackground()
            verify(exactly = 0) { analyticsKeySubmissionStorage.submittedInBackground }
            collector.reportAdvancedConsentGiven()
            verify(exactly = 0) { analyticsKeySubmissionStorage.advancedConsentGiven }
            collector.reportConsentWithdrawn()
            verify(exactly = 0) { analyticsKeySubmissionStorage.advancedConsentGiven }
            collector.reportLastSubmissionFlowScreen(Screen.UNKNOWN)
            verify(exactly = 0) { analyticsKeySubmissionStorage.lastSubmissionFlowScreen }
            collector.reportPositiveTestResultReceived()
            verify(exactly = 0) { analyticsKeySubmissionStorage.testResultReceivedAt }
            collector.reportSubmittedAfterCancel()
            verify(exactly = 0) { analyticsKeySubmissionStorage.submittedAfterCancel }
        }
    }

    fun createInstance() = AnalyticsKeySubmissionCollector(
        timeStamper,
        analyticsSettings,
        analyticsKeySubmissionStorage,
        riskLevelStorage,
        riskLevelSettings
    )
}
