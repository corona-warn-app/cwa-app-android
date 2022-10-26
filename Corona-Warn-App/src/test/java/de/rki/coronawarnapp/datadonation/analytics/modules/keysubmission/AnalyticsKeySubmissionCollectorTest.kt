package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.LastCombinedRiskResults
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateUtc
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference
import java.time.Duration
import java.time.Instant

class AnalyticsKeySubmissionCollectorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var analyticsPcrKeySubmissionStorage: AnalyticsPCRKeySubmissionStorage
    @MockK lateinit var analyticsRaKeySubmissionStorage: AnalyticsRAKeySubmissionStorage
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var ewRiskLevelResult: EwRiskLevelResult
    @MockK lateinit var ptRiskLevelResult: PtRiskLevelResult

    private val now = Instant.now()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns now
    }

    @Test
    fun `save test registered`() {
        val combinedEwPtRiskLevelResult = CombinedEwPtRiskLevelResult(ptRiskLevelResult, ewRiskLevelResult)
        coEvery { analyticsSettings.analyticsEnabled.value } returns true

        every { ewRiskLevelResult.riskState } returns RiskState.INCREASED_RISK
        every { ptRiskLevelResult.riskState } returns RiskState.LOW_RISK
        every { ewRiskLevelResult.calculatedAt } returns now
        every { ptRiskLevelResult.calculatedAt } returns now
        every { ewRiskLevelResult.wasSuccessfullyCalculated } returns true
        every { ptRiskLevelResult.wasSuccessfullyCalculated } returns true

        coEvery {
            riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult
        } returns flowOf(LastCombinedRiskResults(combinedEwPtRiskLevelResult, RiskState.INCREASED_RISK))

        coEvery { riskLevelStorage.allEwRiskLevelResultsWithExposureWindows } returns flowOf(listOf(ewRiskLevelResult))
        coEvery { riskLevelStorage.allEwRiskLevelResults } returns flowOf(listOf(ewRiskLevelResult))
        coEvery { riskLevelStorage.allPtRiskLevelResults } returns flowOf(listOf(ptRiskLevelResult))

        val pcrTestRegisteredAt = mockFlowPreference(now.toEpochMilli())
        coEvery { analyticsPcrKeySubmissionStorage.testRegisteredAt } returns pcrTestRegisteredAt

        val raTestRegisteredAt = mockFlowPreference(now.toEpochMilli())
        coEvery { analyticsRaKeySubmissionStorage.testRegisteredAt } returns raTestRegisteredAt

        every { ewRiskLevelResult.wasSuccessfullyCalculated } returns true

        val hoursSinceHighRiskWarningAtTestRegistration = mockFlowPreference(-1)
        every { analyticsPcrKeySubmissionStorage.ewHoursSinceHighRiskWarningAtTestRegistration } returns
            hoursSinceHighRiskWarningAtTestRegistration
        every { analyticsRaKeySubmissionStorage.ewHoursSinceHighRiskWarningAtTestRegistration } returns
            hoursSinceHighRiskWarningAtTestRegistration

        val daysSinceMostRecentDateAtRiskLevelAtTestRegistration = mockFlowPreference(0)
        every { analyticsPcrKeySubmissionStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration
        every { analyticsRaKeySubmissionStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration
        every { analyticsPcrKeySubmissionStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration
        every { analyticsRaKeySubmissionStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration

        every { analyticsPcrKeySubmissionStorage.clear() } just Runs
        every { analyticsRaKeySubmissionStorage.clear() } just Runs

        every { ewRiskLevelResult.mostRecentDateAtRiskState } returns now.minus(Duration.ofDays(2))
        every { ptRiskLevelResult.mostRecentDateAtRiskState } returns
            now.minus(Duration.ofDays(2)).toLocalDateUtc()

        runTest {
            val collector = createInstance()
            collector.reportTestRegistered(PCR)
            verify { pcrTestRegisteredAt.update(any()) }
            verify { hoursSinceHighRiskWarningAtTestRegistration.update(any()) }
            verify { daysSinceMostRecentDateAtRiskLevelAtTestRegistration.update(any()) }
        }

        runTest {
            val collector = createInstance()
            collector.reportTestRegistered(RAPID_ANTIGEN)
            verify { raTestRegisteredAt.update(any()) }
            verify { hoursSinceHighRiskWarningAtTestRegistration.update(any()) }
            verify { daysSinceMostRecentDateAtRiskLevelAtTestRegistration.update(any()) }
        }
    }

    @Test
    fun `PCR save keys submitted`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val submittedFlow = mockFlowPreference(false)
        every { analyticsPcrKeySubmissionStorage.submitted } returns submittedFlow
        val submittedAtFlow = mockFlowPreference(now.toEpochMilli())
        every { analyticsPcrKeySubmissionStorage.submittedAt } returns submittedAtFlow
        runTest {
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
        runTest {
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
        runTest {
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
        runTest {
            val collector = createInstance()
            collector.reportSubmittedAfterSymptomFlow(PCR)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `PCR save positive test result received`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(-1L)
        every { analyticsPcrKeySubmissionStorage.testResultReceivedAt } returns flow

        runTest {
            val collector = createInstance()
            collector.reportPositiveTestResultReceived(PCR)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `PCR positive test result received is not overwritten`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(now.toEpochMilli())
        every { analyticsPcrKeySubmissionStorage.testResultReceivedAt } returns flow

        runTest {
            val collector = createInstance()
            collector.reportPositiveTestResultReceived(PCR)
            verify(exactly = 0) { flow.update(any()) }
        }
    }

    @Test
    fun `PCR save advanced consent given`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns true
        val flow = mockFlowPreference(false)
        every { analyticsPcrKeySubmissionStorage.advancedConsentGiven } returns flow
        runTest {
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
        runTest {
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
        runTest {
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
        runTest {
            val collector = createInstance()
            collector.reportLastSubmissionFlowScreen(Screen.WARN_OTHERS, PCR)
            verify { flow.update(any()) }
        }
    }

    @Test
    fun `PCR no data collection if disabled`() {
        coEvery { analyticsSettings.analyticsEnabled.value } returns false
        runTest {
            val collector = createInstance()
            collector.reportTestRegistered(PCR)
            verify(exactly = 0) { analyticsPcrKeySubmissionStorage.testRegisteredAt }
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
    )
}
