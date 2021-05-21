package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.PCR
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
    @MockK lateinit var analyticsPcrKeySubmissionStorage: AnalyticsPCRKeySubmissionStorage
    @MockK lateinit var analyticsRaKeySubmissionStorage: AnalyticsRAKeySubmissionStorage
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
        coEvery { analyticsPcrKeySubmissionStorage.testRegisteredAt } returns testRegisteredAt
        coEvery { analyticsRaKeySubmissionStorage.testRegisteredAt } returns testRegisteredAt
        every { ewRiskLevelResult.wasSuccessfullyCalculated } returns true
        val riskLevelAtTestRegistration = mockFlowPreference(-1)
        every { analyticsPcrKeySubmissionStorage.riskLevelAtTestRegistration } returns riskLevelAtTestRegistration
        every { analyticsRaKeySubmissionStorage.riskLevelAtTestRegistration } returns riskLevelAtTestRegistration
        val hoursSinceHighRiskWarningAtTestRegistration = mockFlowPreference(-1)
        every { analyticsPcrKeySubmissionStorage.ewHoursSinceHighRiskWarningAtTestRegistration } returns
            hoursSinceHighRiskWarningAtTestRegistration
        every { analyticsRaKeySubmissionStorage.ewHoursSinceHighRiskWarningAtTestRegistration } returns
            hoursSinceHighRiskWarningAtTestRegistration
        coEvery {
            riskLevelSettings.ewLastChangeCheckedRiskLevelTimestamp
        } returns now
            .minus(Days.days(2).toStandardDuration()).toDateTime().toLocalDate()
            .toDateTime(LocalTime(22, 0)).toInstant()
        val daysSinceMostRecentDateAtRiskLevelAtTestRegistration = mockFlowPreference(0)
        every { analyticsPcrKeySubmissionStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration
        every { analyticsRaKeySubmissionStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration
        every { analyticsPcrKeySubmissionStorage.clear() } just Runs
        every { analyticsRaKeySubmissionStorage.clear() } just Runs
        runBlockingTest {
            val collector = createInstance()
            collector.reportTestRegistered(PCR)
            verify { testRegisteredAt.update(any()) }
            verify { riskLevelAtTestRegistration.update(any()) }
            verify { hoursSinceHighRiskWarningAtTestRegistration.update(any()) }
            verify { daysSinceMostRecentDateAtRiskLevelAtTestRegistration.update(any()) }
        }
        runBlockingTest {
            val collector = createInstance()
            collector.reportTestRegistered(CoronaTest.Type.RAPID_ANTIGEN)
            verify { testRegisteredAt.update(any()) }
            verify { riskLevelAtTestRegistration.update(any()) }
            verify { hoursSinceHighRiskWarningAtTestRegistration.update(any()) }
            verify { daysSinceMostRecentDateAtRiskLevelAtTestRegistration.update(any()) }
        }
    }

    @Test
    fun `PCR save keys submitted`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val submittedFlow = mockFlowPreference(false)
        every { analyticsPcrKeySubmissionStorage.submitted } returns submittedFlow
        val submittedAtFlow = mockFlowPreference(now.millis)
        every { analyticsPcrKeySubmissionStorage.submittedAt } returns submittedAtFlow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmitted(PCR)
            verify { submittedFlow.update(any()) }
            verify { submittedAtFlow.update(any()) }
        }
    }

    @Test
    fun `PCR save keys submitted after cancel`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsPcrKeySubmissionStorage.submittedAfterCancel } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmittedAfterCancel(PCR)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `PCR save keys submitted in background`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsPcrKeySubmissionStorage.submittedInBackground } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmittedInBackground(PCR)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `PCR save keys submitted after symptom flow`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsPcrKeySubmissionStorage.submittedAfterSymptomFlow } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportSubmittedAfterSymptomFlow(PCR)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `PCR save positive test result received`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(now.millis)
        every { analyticsPcrKeySubmissionStorage.testResultReceivedAt } returns flow

        runBlockingTest {
            val collector = createInstance()
            collector.reportPositiveTestResultReceived(PCR)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `PCR save advanced consent given`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsPcrKeySubmissionStorage.advancedConsentGiven } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportAdvancedConsentGiven(PCR)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `PCR save consent withdrawn`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsPcrKeySubmissionStorage.advancedConsentGiven } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportConsentWithdrawn(PCR)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `save registered with tele tan`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsPcrKeySubmissionStorage.registeredWithTeleTAN } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportRegisteredWithTeleTAN()
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `PCR save last submission flow screen`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(0)
        every { analyticsPcrKeySubmissionStorage.lastSubmissionFlowScreen } returns flow
        runBlockingTest {
            val collector = createInstance()
            collector.reportLastSubmissionFlowScreen(Screen.WARN_OTHERS, PCR)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `PCR no data collection if disabled`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns false
        runBlockingTest {
            val collector = createInstance()
            collector.reportTestRegistered(PCR)
            verify(exactly = 0) { analyticsPcrKeySubmissionStorage.testRegisteredAt }
            verify(exactly = 0) { analyticsPcrKeySubmissionStorage.riskLevelAtTestRegistration }
            verify(exactly = 0) { analyticsPcrKeySubmissionStorage.ewHoursSinceHighRiskWarningAtTestRegistration }
            collector.reportSubmitted(PCR)
            verify(exactly = 0) { analyticsPcrKeySubmissionStorage.submitted }
            collector.reportSubmittedInBackground(PCR)
            verify(exactly = 0) { analyticsPcrKeySubmissionStorage.submittedInBackground }
            collector.reportAdvancedConsentGiven(PCR)
            verify(exactly = 0) { analyticsPcrKeySubmissionStorage.advancedConsentGiven }
            collector.reportConsentWithdrawn(PCR)
            verify(exactly = 0) { analyticsPcrKeySubmissionStorage.advancedConsentGiven }
            collector.reportLastSubmissionFlowScreen(Screen.UNKNOWN, PCR)
            verify(exactly = 0) { analyticsPcrKeySubmissionStorage.lastSubmissionFlowScreen }
            collector.reportPositiveTestResultReceived(PCR)
            verify(exactly = 0) { analyticsPcrKeySubmissionStorage.testResultReceivedAt }
            collector.reportSubmittedAfterCancel(PCR)
            verify(exactly = 0) { analyticsPcrKeySubmissionStorage.submittedAfterCancel }
        }
    }

    fun createInstance() = AnalyticsKeySubmissionCollector(
        timeStamper,
        analyticsSettings,
        analyticsPcrKeySubmissionStorage,
        analyticsRaKeySubmissionStorage,
        riskLevelStorage,
        riskLevelSettings
    )
}
