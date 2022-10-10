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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
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
        every { analyticsSettings.analyticsEnabled } returns flowOf(true)
    }

    @Test
    fun `save test registered`() = runTest2 {
        val combinedEwPtRiskLevelResult = CombinedEwPtRiskLevelResult(ptRiskLevelResult, ewRiskLevelResult)

        coEvery { analyticsPcrKeySubmissionStorage.updateTestRegisteredAt(any()) } just Runs
        coEvery {
            analyticsPcrKeySubmissionStorage.updateEwHoursSinceHighRiskWarningAtTestRegistration(any())
        } just Runs
        coEvery {
            analyticsPcrKeySubmissionStorage.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any())
        } just Runs
        coEvery {
            analyticsPcrKeySubmissionStorage.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any())
        } just Runs

        coEvery { analyticsRaKeySubmissionStorage.updateTestRegisteredAt(any()) } just Runs
        coEvery {
            analyticsRaKeySubmissionStorage.updateEwHoursSinceHighRiskWarningAtTestRegistration(any())
        } just Runs
        coEvery {
            analyticsRaKeySubmissionStorage.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any())
        } just Runs
        coEvery {
            analyticsRaKeySubmissionStorage.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any())
        } just Runs

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

        coEvery { analyticsPcrKeySubmissionStorage.testRegisteredAt } returns flowOf(now.toEpochMilli())

        coEvery { analyticsRaKeySubmissionStorage.testRegisteredAt } returns flowOf(now.toEpochMilli())

        every { ewRiskLevelResult.wasSuccessfullyCalculated } returns true

        every { analyticsPcrKeySubmissionStorage.ewHoursSinceHighRiskWarningAtTestRegistration } returns
            flowOf(-1)
        every { analyticsRaKeySubmissionStorage.ewHoursSinceHighRiskWarningAtTestRegistration } returns
            flowOf(-1)

        every { analyticsPcrKeySubmissionStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            flowOf(0)
        every { analyticsRaKeySubmissionStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            flowOf(0)
        every { analyticsPcrKeySubmissionStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            flowOf(0)
        every { analyticsRaKeySubmissionStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns
            flowOf(0)

        coEvery { analyticsPcrKeySubmissionStorage.clear() } just Runs
        coEvery { analyticsRaKeySubmissionStorage.clear() } just Runs

        every { ewRiskLevelResult.mostRecentDateAtRiskState } returns now.minus(Duration.ofDays(2))
        every { ptRiskLevelResult.mostRecentDateAtRiskState } returns
            now.minus(Duration.ofDays(2)).toLocalDateUtc()

        val collector = createInstance(this)
        collector.reportTestRegistered(PCR)
        coVerify {
            analyticsPcrKeySubmissionStorage.updateTestRegisteredAt(any())
            analyticsPcrKeySubmissionStorage.updateEwHoursSinceHighRiskWarningAtTestRegistration(any())
            analyticsPcrKeySubmissionStorage.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any())
            analyticsPcrKeySubmissionStorage.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any())
        }

        val collector2 = createInstance(this)
        collector2.reportTestRegistered(RAPID_ANTIGEN)
        coVerify {
            analyticsRaKeySubmissionStorage.updateTestRegisteredAt(any())
            analyticsRaKeySubmissionStorage.updateEwHoursSinceHighRiskWarningAtTestRegistration(any())
            analyticsRaKeySubmissionStorage.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any())
        }
    }

    @Test
    fun `PCR save keys submitted`() = runTest2 {
        coEvery { analyticsPcrKeySubmissionStorage.updateSubmitted(any()) } just Runs
        coEvery { analyticsPcrKeySubmissionStorage.updateSubmittedAt(any()) } just Runs
        every { analyticsPcrKeySubmissionStorage.submitted } returns flowOf(false)
        every { analyticsPcrKeySubmissionStorage.submittedAt } returns flowOf(now.toEpochMilli())

        val collector = createInstance(this)
        collector.reportSubmitted(PCR)
        coVerify {
            analyticsPcrKeySubmissionStorage.updateSubmitted(any())
            analyticsPcrKeySubmissionStorage.updateSubmittedAt(any())
        }
    }

    @Test
    fun `PCR save keys submitted after cancel`() = runTest2 {
        every { analyticsPcrKeySubmissionStorage.submittedAfterCancel } returns flowOf(false)
        coEvery { analyticsPcrKeySubmissionStorage.updateSubmittedAfterCancel(any()) } just Runs

        val collector = createInstance(this)
        collector.reportSubmittedAfterCancel(PCR)
        coVerify { analyticsPcrKeySubmissionStorage.updateSubmittedAfterCancel(any()) }
    }

    @Test
    fun `PCR save keys submitted in background`() = runTest2 {
        every { analyticsPcrKeySubmissionStorage.submittedInBackground } returns flowOf(false)
        coEvery { analyticsPcrKeySubmissionStorage.updateSubmittedInBackground(any()) } just Runs

        val collector = createInstance(this)
        collector.reportSubmittedInBackground(PCR)
        coVerify { analyticsPcrKeySubmissionStorage.updateSubmittedInBackground(any()) }
    }

    @Test
    fun `PCR save keys submitted after symptom flow`() = runTest2 {
        every { analyticsPcrKeySubmissionStorage.submittedAfterSymptomFlow } returns flowOf(false)
        coEvery { analyticsPcrKeySubmissionStorage.updateSubmittedAfterSymptomFlow(any()) } just Runs

        val collector = createInstance(this)
        collector.reportSubmittedAfterSymptomFlow(PCR)
        coVerify { analyticsPcrKeySubmissionStorage.updateSubmittedAfterSymptomFlow(any()) }
    }

    @Test
    fun `PCR save positive test result received`() = runTest2 {
        every { analyticsPcrKeySubmissionStorage.testResultReceivedAt } returns flowOf(-1L)
        coEvery { analyticsPcrKeySubmissionStorage.updateTestResultReceivedAt(any()) } just Runs

        val collector = createInstance(this)
        collector.reportPositiveTestResultReceived(PCR)
        coVerify { analyticsPcrKeySubmissionStorage.updateTestResultReceivedAt(any()) }
    }

    @Test
    fun `PCR positive test result received is not overwritten`() = runTest2 {
        every { analyticsPcrKeySubmissionStorage.testResultReceivedAt } returns flowOf(now.toEpochMilli())

        val collector = createInstance(this)
        collector.reportPositiveTestResultReceived(PCR)
        coVerify(exactly = 0) { analyticsPcrKeySubmissionStorage.updateTestResultReceivedAt(any()) }
    }

    @Test
    fun `PCR save advanced consent given`() = runTest2 {
        every { analyticsPcrKeySubmissionStorage.advancedConsentGiven } returns flowOf(false)
        coEvery { analyticsPcrKeySubmissionStorage.updateAdvancedConsentGiven(any()) } just Runs

        val collector = createInstance(this)
        collector.reportAdvancedConsentGiven(PCR)
        coVerify { analyticsPcrKeySubmissionStorage.updateAdvancedConsentGiven(any()) }
    }

    @Test
    fun `PCR save consent withdrawn`() = runTest2 {
        every { analyticsPcrKeySubmissionStorage.advancedConsentGiven } returns flowOf(false)
        coEvery { analyticsPcrKeySubmissionStorage.updateAdvancedConsentGiven(any()) } just Runs

        val collector = createInstance(this)
        collector.reportConsentWithdrawn(PCR)
        coVerify { analyticsPcrKeySubmissionStorage.updateAdvancedConsentGiven(any()) }
    }

    @Test
    fun `save registered with tele tan`() = runTest2 {
        every { analyticsPcrKeySubmissionStorage.registeredWithTeleTAN } returns flowOf(false)
        coEvery { analyticsPcrKeySubmissionStorage.updateRegisteredWithTeleTAN(any()) } just Runs

        val collector = createInstance(this)
        collector.reportRegisteredWithTeleTAN()
        coVerify { analyticsPcrKeySubmissionStorage.updateRegisteredWithTeleTAN(any()) }
    }

    @Test
    fun `PCR save last submission flow screen`() = runTest2 {
        coEvery { analyticsPcrKeySubmissionStorage.updateLastSubmissionFlowScreen(any()) } just Runs
        every { analyticsPcrKeySubmissionStorage.lastSubmissionFlowScreen } returns flowOf(0)

        val collector = createInstance(this)
        collector.reportLastSubmissionFlowScreen(Screen.WARN_OTHERS, PCR)
        coVerify { analyticsPcrKeySubmissionStorage.updateLastSubmissionFlowScreen(any()) }
    }

    @Test
    fun `PCR no data collection if disabled`() = runTest2 {
        every { analyticsSettings.analyticsEnabled } returns flowOf(false)

        val collector = createInstance(this)
        collector.reportTestRegistered(PCR)
        verify(exactly = 0) {
            analyticsPcrKeySubmissionStorage.testRegisteredAt
            analyticsPcrKeySubmissionStorage.ewHoursSinceHighRiskWarningAtTestRegistration
        }
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

    fun createInstance(scope: CoroutineScope) = AnalyticsKeySubmissionCollector(
        timeStamper,
        analyticsSettings,
        analyticsPcrKeySubmissionStorage,
        analyticsRaKeySubmissionStorage,
        riskLevelStorage,
        scope
    )
}
