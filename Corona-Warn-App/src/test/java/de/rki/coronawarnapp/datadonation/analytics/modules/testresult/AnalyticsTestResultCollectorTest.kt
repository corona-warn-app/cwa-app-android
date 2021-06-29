package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_REDEEMED
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.LastCombinedRiskResults
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class AnalyticsTestResultCollectorTest : BaseTest() {

    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var pcrTestResultSettings: AnalyticsPCRTestResultSettings
    @MockK lateinit var raTestResultSettings: AnalyticsRATestResultSettings
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var combinedResult: CombinedEwPtRiskLevelResult
    @MockK lateinit var ewRiskLevelResult: EwRiskLevelResult
    @MockK lateinit var ptRiskLevelResult: PtRiskLevelResult

    private lateinit var analyticsTestResultCollector: AnalyticsTestResultCollector

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.parse("2021-03-02T09:57:11+01:00")
        every { pcrTestResultSettings.clear() } just Runs
        every { raTestResultSettings.clear() } just Runs

        val lastCombinedResults = LastCombinedRiskResults(combinedResult, combinedResult)
        every { combinedResult.ewRiskLevelResult } returns ewRiskLevelResult
        every { combinedResult.ptRiskLevelResult } returns ptRiskLevelResult
        every { ewRiskLevelResult.riskState } returns RiskState.LOW_RISK
        every { ptRiskLevelResult.riskState } returns RiskState.LOW_RISK
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns
            flowOf(lastCombinedResults)

        analyticsTestResultCollector = AnalyticsTestResultCollector(
            analyticsSettings,
            pcrTestResultSettings,
            raTestResultSettings,
            riskLevelStorage,
            timeStamper,
        )
    }

    @Test
    fun `saveTestResultAnalyticsSettings does not save anything when no user consent`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
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
    fun `saveTestResult saves data when user gave consent`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)

            // PCR
            every { pcrTestResultSettings.testResult } returns
                mockFlowPreference(null)
            every { pcrTestResultSettings.finalTestResultReceivedAt } returns
                mockFlowPreference(Instant.EPOCH)

            analyticsTestResultCollector.reportTestResultReceived(PCR_POSITIVE, PCR)

            verify {
                analyticsSettings.analyticsEnabled
                pcrTestResultSettings.testResult
                pcrTestResultSettings.finalTestResultReceivedAt
            }

            // RAT
            every { raTestResultSettings.testResult } returns
                mockFlowPreference(null)
            every { raTestResultSettings.finalTestResultReceivedAt } returns
                mockFlowPreference(Instant.EPOCH)

            analyticsTestResultCollector.reportTestResultReceived(RAT_POSITIVE, RAPID_ANTIGEN)

            verify {
                analyticsSettings.analyticsEnabled
                raTestResultSettings.testResult
                raTestResultSettings.finalTestResultReceivedAt
            }
        }

    @Test
    fun `saveTestResultAnalyticsSettings does not save data when TestResult is INVALID`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
            analyticsTestResultCollector.reportTestResultReceived(PCR_INVALID, PCR)
            analyticsTestResultCollector.reportTestResultReceived(RAT_INVALID, RAPID_ANTIGEN)

            verify {
                analyticsSettings.analyticsEnabled wasNot Called
            }
        }

    @Test
    fun `saveTestResultAnalyticsSettings does not save data when TestResult is REDEEMED`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
            analyticsTestResultCollector.reportTestResultReceived(PCR_REDEEMED, PCR)
            analyticsTestResultCollector.reportTestResultReceived(RAT_REDEEMED, RAPID_ANTIGEN)
            verify {
                analyticsSettings.analyticsEnabled wasNot Called
            }
        }

    @Test
    fun `reportTestResultReceived doesn't update when TestResult isn't POS or NEG`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
            every { pcrTestResultSettings.testResult } returns mockFlowPreference(
                PCR_OR_RAT_PENDING
            )
            for (testResult in listOf(PCR_REDEEMED, PCR_INVALID, PCR_OR_RAT_PENDING)) {
                analyticsTestResultCollector.reportTestResultReceived(testResult, PCR)

                verify {
                    analyticsSettings.analyticsEnabled
                    pcrTestResultSettings.finalTestResultReceivedAt wasNot Called
                }
            }

            every { raTestResultSettings.testResult } returns mockFlowPreference(
                PCR_OR_RAT_PENDING
            )
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
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
            every { pcrTestResultSettings.testResult } returns mockFlowPreference(PCR_OR_RAT_PENDING)
            every { pcrTestResultSettings.finalTestResultReceivedAt } returns
                mockFlowPreference(Instant.parse("2021-03-02T09:57:11+01:00"))
            analyticsTestResultCollector.reportTestResultReceived(PCR_NEGATIVE, PCR)

            verify {
                analyticsSettings.analyticsEnabled
                pcrTestResultSettings.testResult wasNot Called
                pcrTestResultSettings.finalTestResultReceivedAt wasNot Called
                pcrTestResultSettings.testResult wasNot Called
            }

            every { raTestResultSettings.testResult } returns mockFlowPreference(PCR_OR_RAT_PENDING)
            every { raTestResultSettings.finalTestResultReceivedAt } returns
                mockFlowPreference(Instant.parse("2021-03-02T09:57:11+01:00"))
            analyticsTestResultCollector.reportTestResultReceived(RAT_NEGATIVE, RAPID_ANTIGEN)

            verify {
                analyticsSettings.analyticsEnabled
                raTestResultSettings.testResult wasNot Called
                raTestResultSettings.finalTestResultReceivedAt wasNot Called
                raTestResultSettings.testResult wasNot Called
            }
        }

    @Test
    fun `updatePendingTestResultReceivedTime update when TestResult is POS or NEG`() =
        runBlockingTest {
            for (testResult in listOf(PCR_NEGATIVE, PCR_POSITIVE)) {
                every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
                every { pcrTestResultSettings.testResult } returns mockFlowPreference(
                    PCR_OR_RAT_PENDING
                )
                every { pcrTestResultSettings.finalTestResultReceivedAt } returns
                    mockFlowPreference(Instant.EPOCH)

                analyticsTestResultCollector.reportTestResultReceived(testResult, PCR)

                verify {
                    analyticsSettings.analyticsEnabled
                    pcrTestResultSettings.finalTestResultReceivedAt
                }
            }

            for (testResult in listOf(RAT_NEGATIVE, RAT_POSITIVE)) {
                every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
                every { raTestResultSettings.testResult } returns mockFlowPreference(
                    PCR_OR_RAT_PENDING
                )
                every { raTestResultSettings.finalTestResultReceivedAt } returns mockFlowPreference(Instant.EPOCH)
                analyticsTestResultCollector.reportTestResultReceived(testResult, RAPID_ANTIGEN)

                verify {
                    analyticsSettings.analyticsEnabled
                    pcrTestResultSettings.finalTestResultReceivedAt
                }
            }
        }

    @Test
    fun `clear is clearing saved data`() {
        analyticsTestResultCollector.clear(PCR)
        verify {
            pcrTestResultSettings.clear()
        }
        analyticsTestResultCollector.clear(RAPID_ANTIGEN)
        verify {
            raTestResultSettings.clear()
        }
    }
}
