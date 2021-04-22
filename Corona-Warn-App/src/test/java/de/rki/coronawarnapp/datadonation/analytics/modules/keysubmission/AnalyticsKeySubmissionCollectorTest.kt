package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Instant
import org.joda.time.LocalTime
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
    @MockK lateinit var ewRiskLevelResult: EwRiskLevelResult

    private val now = Instant.now()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns now
    }

    @Test
    fun `save test registered`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        every { ewRiskLevelResult.riskState } returns RiskState.INCREASED_RISK
        coEvery {
            riskLevelStorage.latestAndLastSuccessfulEwRiskLevelResult
        } returns flowOf(listOf(ewRiskLevelResult))
        every { riskLevelSettings.lastChangeToHighRiskLevelTimestamp } returns now.minus(
            Hours.hours(2).toStandardDuration()
        )
        val testRegisteredAt = mockFlowPreference(now.millis)
        coEvery { analyticsKeySubmissionStorage.testRegisteredAt } returns testRegisteredAt
        every { ewRiskLevelResult.wasSuccessfullyCalculated } returns true
        val riskLevelAtTestRegistration = mockFlowPreference(-1)
        every { analyticsKeySubmissionStorage.riskLevelAtTestRegistration } returns riskLevelAtTestRegistration
        val hoursSinceHighRiskWarningAtTestRegistration = mockFlowPreference(-1)
        every { analyticsKeySubmissionStorage.hoursSinceHighRiskWarningAtTestRegistration } returns
            hoursSinceHighRiskWarningAtTestRegistration
        coEvery {
            riskLevelSettings.lastChangeCheckedRiskLevelTimestamp
        } returns now
            .minus(Days.days(2).toStandardDuration()).toDateTime().toLocalDate()
            .toDateTime(LocalTime(22, 0)).toInstant()
        val daysSinceMostRecentDateAtRiskLevelAtTestRegistration = mockFlowPreference(0)
        every { analyticsKeySubmissionStorage.daysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration
        every { analyticsKeySubmissionStorage.clear() } just Runs
        runBlockingTest {
            val collector = createInstance()
            collector.reportPcrTestRegistered()
            verify { testRegisteredAt.update(any()) }
            verify { riskLevelAtTestRegistration.update(any()) }
            verify { hoursSinceHighRiskWarningAtTestRegistration.update(any()) }
            verify { daysSinceMostRecentDateAtRiskLevelAtTestRegistration.update(any()) }
        }
    }

    @Test
    fun `save keys submitted`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val submittedFlow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.submitted } returns submittedFlow
        val submittedAtFlow = mockFlowPreference(now.millis)
        every { analyticsKeySubmissionStorage.submittedAt } returns submittedAtFlow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmittedPcr()
            verify { submittedFlow.update(any()) }
            verify { submittedAtFlow.update(any()) }
        }
    }

    @Test
    fun `save keys submitted after cancel`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.submittedAfterCancel } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmittedAfterCancelPcr()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `save keys submitted in background`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.submittedInBackground } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmittedInBackgroundPcr()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `save keys submitted after symptom flow`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.submittedAfterSymptomFlow } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmittedAfterSymptomFlowPcr()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `save positive test result received`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(now.millis)
        every { analyticsKeySubmissionStorage.testResultReceivedAt } returns flow

        runBlockingTest {
            val collector = createInstance()
            collector.reportPositivePcrTestResultReceived()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `save advanced consent given`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.advancedConsentGiven } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportAdvancedConsentGivenPcr()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `save consent withdrawn`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.advancedConsentGiven } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportConsentWithdrawnPcr()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `save registered with tele tan`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsKeySubmissionStorage.registeredWithTeleTAN } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportPcrTestRegisteredWithTeleTan()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `save last submission flow screen`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(0)
        every { analyticsKeySubmissionStorage.lastSubmissionFlowScreen } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportLastSubmissionFlowScreenPcr(Screen.WARN_OTHERS)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `no data collection if disabled`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns false
        runBlockingTest {
            val collector = createInstance()
            collector.reportPcrTestRegistered()
            verify(exactly = 0) { analyticsKeySubmissionStorage.testRegisteredAt }
            verify(exactly = 0) { analyticsKeySubmissionStorage.riskLevelAtTestRegistration }
            verify(exactly = 0) { analyticsKeySubmissionStorage.hoursSinceHighRiskWarningAtTestRegistration }
            collector.reportSubmittedPcr()
            verify(exactly = 0) { analyticsKeySubmissionStorage.submitted }
            collector.reportSubmittedInBackgroundPcr()
            verify(exactly = 0) { analyticsKeySubmissionStorage.submittedInBackground }
            collector.reportAdvancedConsentGivenPcr()
            verify(exactly = 0) { analyticsKeySubmissionStorage.advancedConsentGiven }
            collector.reportConsentWithdrawnPcr()
            verify(exactly = 0) { analyticsKeySubmissionStorage.advancedConsentGiven }
            collector.reportLastSubmissionFlowScreenPcr(Screen.UNKNOWN)
            verify(exactly = 0) { analyticsKeySubmissionStorage.lastSubmissionFlowScreen }
            collector.reportPositivePcrTestResultReceived()
            verify(exactly = 0) { analyticsKeySubmissionStorage.testResultReceivedAt }
            collector.reportSubmittedAfterCancelPcr()
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
