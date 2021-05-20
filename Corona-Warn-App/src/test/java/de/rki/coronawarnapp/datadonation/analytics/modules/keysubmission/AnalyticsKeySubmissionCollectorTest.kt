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
        every { riskLevelSettings.ewLastChangeToHighRiskLevelTimestamp } returns now.minus(
            Hours.hours(2).toStandardDuration()
        )
        val testRegisteredAt = mockFlowPreference(now.millis)
        coEvery { analyticsKeySubmissionStorage.testRegisteredAt } returns testRegisteredAt
        every { ewRiskLevelResult.wasSuccessfullyCalculated } returns true
        val riskLevelAtTestRegistration = mockFlowPreference(-1)
        every { analyticsKeySubmissionStorage.riskLevelAtTestRegistration } returns riskLevelAtTestRegistration
        val hoursSinceHighRiskWarningAtTestRegistration = mockFlowPreference(-1)
        every { analyticsKeySubmissionStorage.ewHoursSinceHighRiskWarningAtTestRegistration } returns
            hoursSinceHighRiskWarningAtTestRegistration
        coEvery {
            riskLevelSettings.ewLastChangeCheckedRiskLevelTimestamp
        } returns now
            .minus(Days.days(2).toStandardDuration()).toDateTime().toLocalDate()
            .toDateTime(LocalTime(22, 0)).toInstant()
        val daysSinceMostRecentDateAtRiskLevelAtTestRegistration = mockFlowPreference(0)
        every { analyticsKeySubmissionStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration
        every { analyticsKeySubmissionStorage.clear() } just Runs
        runBlockingTest {
            val collector = createInstance()
            collector.reportTestRegistered()
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
            collector.reportSubmitted()
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
            collector.reportSubmittedAfterCancel()
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
            collector.reportSubmittedInBackground()
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
            collector.reportSubmittedAfterSymptomFlow()
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
            collector.reportPositiveTestResultReceived()
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
            collector.reportAdvancedConsentGiven()
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
            collector.reportConsentWithdrawn()
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
            collector.reportRegisteredWithTeleTAN()
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
            collector.reportLastSubmissionFlowScreen(Screen.WARN_OTHERS)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `no data collection if disabled`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns false
        runBlockingTest {
            val collector = createInstance()
            collector.reportTestRegistered()
            verify(exactly = 0) { analyticsKeySubmissionStorage.testRegisteredAt }
            verify(exactly = 0) { analyticsKeySubmissionStorage.riskLevelAtTestRegistration }
            verify(exactly = 0) { analyticsKeySubmissionStorage.ewHoursSinceHighRiskWarningAtTestRegistration }
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
