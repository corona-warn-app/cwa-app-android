package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.datadonation.analytics.common.AnalyticsExposureWindow
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.LastCombinedRiskResults
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateUtc
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.extensions.toInstant
import java.time.Instant
import java.time.OffsetDateTime

class AnalyticsTestResultCollectorTest : BaseTest() {

    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var pcrTestResultSettings: AnalyticsPCRTestResultSettings
    @MockK lateinit var raTestResultSettings: AnalyticsRATestResultSettings
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var combinedResult: CombinedEwPtRiskLevelResult
    @MockK lateinit var ewRiskLevelResult: EwRiskLevelResult
    @MockK lateinit var ptRiskLevelResult: PtRiskLevelResult
    @MockK lateinit var exposureWindowsSettings: AnalyticsExposureWindowsSettings

    @MockK lateinit var analyticsExposureWindow1: AnalyticsExposureWindow
    @MockK lateinit var analyticsExposureWindow2: AnalyticsExposureWindow

    private lateinit var analyticsTestResultCollector: AnalyticsTestResultCollector

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns OffsetDateTime.parse("2021-03-02T09:57:11+01:00").toInstant()
        coEvery { pcrTestResultSettings.clear() } just Runs
        coEvery { raTestResultSettings.clear() } just Runs

        val lastCombinedResults = LastCombinedRiskResults(combinedResult, RiskState.LOW_RISK)
        every { combinedResult.ewRiskLevelResult } returns ewRiskLevelResult
        every { combinedResult.ptRiskLevelResult } returns ptRiskLevelResult
        every { ewRiskLevelResult.riskState } returns RiskState.LOW_RISK
        every { ptRiskLevelResult.riskState } returns RiskState.LOW_RISK
        every { ewRiskLevelResult.mostRecentDateAtRiskState } returns "2021-03-02T09:57:11+01:00".toInstant()
        every { ptRiskLevelResult.mostRecentDateAtRiskState } returns "2021-03-02T09:57:11+01:00".toInstant()
            .toLocalDateUtc()
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns
            flowOf(lastCombinedResults)
        every { exposureWindowsSettings.currentExposureWindows } returns flowOf(null)
        every { pcrTestResultSettings.testRegisteredAt } returns flowOf(timeStamper.nowUTC)
        every { pcrTestResultSettings.exposureWindowsAtTestRegistration } returns flowOf(emptyList())
        every { pcrTestResultSettings.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns flowOf(1)
        every { pcrTestResultSettings.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns flowOf(1)
        every { pcrTestResultSettings.ewHoursSinceHighRiskWarningAtTestRegistration } returns flowOf(1)
        every { pcrTestResultSettings.ptHoursSinceHighRiskWarningAtTestRegistration } returns flowOf(1)
        every { pcrTestResultSettings.ewRiskLevelAtTestRegistration } returns
            flowOf(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
        every { pcrTestResultSettings.ptRiskLevelAtTestRegistration } returns
            flowOf(PpaData.PPARiskLevel.RISK_LEVEL_LOW)

        analyticsTestResultCollector = AnalyticsTestResultCollector(
            analyticsSettings,
            pcrTestResultSettings,
            raTestResultSettings,
            riskLevelStorage,
            timeStamper,
            exposureWindowsSettings
        )
    }

    @Test
    fun `register test collects data`() = runTest {
        every { analyticsSettings.analyticsEnabled } returns flowOf(true)
        coEvery { pcrTestResultSettings.updateTestRegisteredAt(any()) } just Runs
        coEvery { pcrTestResultSettings.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any()) } just Runs
        coEvery { pcrTestResultSettings.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any()) } just Runs
        coEvery { pcrTestResultSettings.updateEwRiskLevelAtTestRegistration(any()) } just Runs
        coEvery { pcrTestResultSettings.updatePtRiskLevelAtTestRegistration(any()) } just Runs
        analyticsTestResultCollector.reportTestRegistered(PCR)

        coVerify(exactly = 1) {
            exposureWindowsSettings.currentExposureWindows
            pcrTestResultSettings.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any())
            pcrTestResultSettings.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any())
            pcrTestResultSettings.updateEwRiskLevelAtTestRegistration(any())
            pcrTestResultSettings.updatePtRiskLevelAtTestRegistration(any())
        }
        coVerify(exactly = 0) {
            pcrTestResultSettings.updateExposureWindowsAtTestRegistration(any())
        }
    }

    @Test
    fun `saveTestResultAnalyticsSettings does not save anything when no user consent`() =
        runTest {
            every { analyticsSettings.analyticsEnabled } returns flowOf(false)
            analyticsTestResultCollector.reportTestResultReceived(PCR_POSITIVE, PCR)

            verify(exactly = 0) {
                pcrTestResultSettings.testResult
                raTestResultSettings.testResult
            }

            analyticsTestResultCollector.reportTestResultReceived(RAT_POSITIVE, RAPID_ANTIGEN)

            verify(exactly = 0) {
                pcrTestResultSettings.testResult
                raTestResultSettings.testResult
            }
        }

    @Test
    fun `saveTestResult saves data when user gave consent`() = runTest {
        every { analyticsSettings.analyticsEnabled } returns flowOf(true)
        coEvery { pcrTestResultSettings.updateTestResult(any()) } just Runs
        coEvery { pcrTestResultSettings.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(any()) } just Runs

        // PCR
        every { pcrTestResultSettings.testResult } returns flowOf(null)
        every { pcrTestResultSettings.finalTestResultReceivedAt } returns flowOf(Instant.EPOCH)

        analyticsTestResultCollector.reportTestResultReceived(PCR_POSITIVE, PCR)

        coVerify(exactly = 1) {
            pcrTestResultSettings.updateTestResult(any())
        }
        coVerify(exactly = 0) {
            pcrTestResultSettings.updateFinalTestResultReceivedAt(any())
        }

        // RAT
        every { raTestResultSettings.testResult } returns flowOf(null)
        every { raTestResultSettings.finalTestResultReceivedAt } returns flowOf(Instant.EPOCH)
        coEvery { raTestResultSettings.updateTestResult(any()) } just Runs

        analyticsTestResultCollector.reportTestResultReceived(RAT_POSITIVE, RAPID_ANTIGEN)

        coVerify(exactly = 1) {
            raTestResultSettings.updateTestResult(any())
        }
        coVerify(exactly = 0) {
            raTestResultSettings.updateFinalTestResultReceivedAt(any())
        }
    }

    @Test
    fun `saveTestResultAnalyticsSettings does not save data when TestResult is INVALID`() =
        runTest {
            every { analyticsSettings.analyticsEnabled } returns flowOf(false)
            analyticsTestResultCollector.reportTestResultReceived(PCR_INVALID, PCR)
            analyticsTestResultCollector.reportTestResultReceived(RAT_INVALID, RAPID_ANTIGEN)

            verify {
                analyticsSettings.analyticsEnabled wasNot Called
            }
        }

    @Test
    fun `saveTestResultAnalyticsSettings does not save data when TestResult is REDEEMED`() =
        runTest {
            every { analyticsSettings.analyticsEnabled } returns flowOf(false)
            analyticsTestResultCollector.reportTestResultReceived(PCR_OR_RAT_REDEEMED, PCR)
            analyticsTestResultCollector.reportTestResultReceived(RAT_REDEEMED, RAPID_ANTIGEN)
            verify {
                analyticsSettings.analyticsEnabled wasNot Called
            }
        }

    @Test
    fun `reportTestResultReceived doesn't update when TestResult isn't POS or NEG`() = runTest {
        every { analyticsSettings.analyticsEnabled } returns flowOf(true)
        every { pcrTestResultSettings.testResult } returns flowOf(PCR_OR_RAT_PENDING)
        coEvery { pcrTestResultSettings.updateTestResult(any()) } just Runs
        coEvery { raTestResultSettings.updateTestResult(any()) } just Runs

        for (testResult in listOf(PCR_OR_RAT_REDEEMED, PCR_INVALID, PCR_OR_RAT_PENDING)) {
            analyticsTestResultCollector.reportTestResultReceived(testResult, PCR)

            verify {
                analyticsSettings.analyticsEnabled
                pcrTestResultSettings.finalTestResultReceivedAt wasNot Called
            }
        }

        every { raTestResultSettings.testResult } returns flowOf(PCR_OR_RAT_PENDING)
        for (testResult in listOf(RAT_REDEEMED, RAT_INVALID, PCR_OR_RAT_PENDING)) {
            analyticsTestResultCollector.reportTestResultReceived(testResult, RAPID_ANTIGEN)

            verify {
                analyticsSettings.analyticsEnabled
                raTestResultSettings.finalTestResultReceivedAt wasNot Called
            }
        }
    }

    @Test
    fun `updatePendingTestResultReceivedTime doesn't update when Test is not scanned after consent`() =
        runTest {
            every { analyticsSettings.analyticsEnabled } returns flowOf(true)
            every { pcrTestResultSettings.testResult } returns flowOf(PCR_OR_RAT_PENDING)
            every { pcrTestResultSettings.finalTestResultReceivedAt } returns
                flowOf(OffsetDateTime.parse("2021-03-02T09:57:11+01:00").toInstant())
            coEvery { pcrTestResultSettings.updateTestResult(any()) } just Runs
            coEvery { raTestResultSettings.updateTestResult(any()) } just Runs
            analyticsTestResultCollector.reportTestResultReceived(PCR_NEGATIVE, PCR)

            verify {
                analyticsSettings.analyticsEnabled
                pcrTestResultSettings.testResult wasNot Called
                pcrTestResultSettings.finalTestResultReceivedAt wasNot Called
                pcrTestResultSettings.testResult wasNot Called
            }

            every { raTestResultSettings.testResult } returns flowOf(PCR_OR_RAT_PENDING)
            every { raTestResultSettings.finalTestResultReceivedAt } returns
                flowOf(OffsetDateTime.parse("2021-03-02T09:57:11+01:00").toInstant())
            analyticsTestResultCollector.reportTestResultReceived(RAT_NEGATIVE, RAPID_ANTIGEN)

            verify {
                analyticsSettings.analyticsEnabled
                raTestResultSettings.testResult wasNot Called
                raTestResultSettings.finalTestResultReceivedAt wasNot Called
                raTestResultSettings.testResult wasNot Called
            }
        }

    @Test
    fun `updatePendingTestResultReceivedTime update when TestResult is POS or NEG`() = runTest {

        every { analyticsSettings.analyticsEnabled } returns flowOf(true)
        every { pcrTestResultSettings.testResult } returns flowOf(PCR_OR_RAT_PENDING)
        every { pcrTestResultSettings.finalTestResultReceivedAt } returns flowOf(Instant.EPOCH)
        every { raTestResultSettings.finalTestResultReceivedAt } returns flowOf(Instant.EPOCH)
        coEvery { pcrTestResultSettings.updateTestResult(any()) } just Runs
        coEvery { raTestResultSettings.updateTestResult(any()) } just Runs

        for (testResult in listOf(PCR_NEGATIVE, PCR_POSITIVE)) {
            analyticsTestResultCollector.reportTestResultReceived(testResult, PCR)

            verify {
                analyticsSettings.analyticsEnabled
                pcrTestResultSettings.finalTestResultReceivedAt
            }
        }

        for (testResult in listOf(RAT_NEGATIVE, RAT_POSITIVE)) {
            analyticsTestResultCollector.reportTestResultReceived(testResult, RAPID_ANTIGEN)

            verify {
                analyticsSettings.analyticsEnabled
                pcrTestResultSettings.finalTestResultReceivedAt
            }
        }
    }

    @Test
    fun `clear is clearing saved data`() = runTest2 {
        analyticsTestResultCollector.clear(PCR)
        coVerify {
            pcrTestResultSettings.clear()
        }
        analyticsTestResultCollector.clear(RAPID_ANTIGEN)
        coVerify {
            raTestResultSettings.clear()
        }
    }

    @Test
    fun `filtering known windows`() {
        every { analyticsExposureWindow1.sha256Hash() } returns "hash1"
        every { analyticsExposureWindow2.sha256Hash() } returns "hash2"

        listOf(analyticsExposureWindow1, analyticsExposureWindow2).filterExposureWindows(
            listOf(analyticsExposureWindow2)
        ) shouldBe listOf(analyticsExposureWindow1)

        listOf(analyticsExposureWindow1, analyticsExposureWindow2).filterExposureWindows(
            listOf()
        ) shouldBe listOf(analyticsExposureWindow1, analyticsExposureWindow2)

        listOf<AnalyticsExposureWindow>().filterExposureWindows(
            listOf(analyticsExposureWindow1, analyticsExposureWindow2)
        ) shouldBe listOf()
    }
}
