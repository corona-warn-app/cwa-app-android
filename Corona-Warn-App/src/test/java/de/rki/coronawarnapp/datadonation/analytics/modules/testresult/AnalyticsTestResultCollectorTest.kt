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
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.LastCombinedRiskResults
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
    @MockK lateinit var pcrTestResultDonorSettings: AnalyticsPCRTestResultSettings
    @MockK lateinit var raTestResultDonorSettings: AnalyticsRATestResultSettings
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var combinedResult: CombinedEwPtRiskLevelResult

    private lateinit var analyticsTestResultCollector: AnalyticsTestResultCollector

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.parse("2021-03-02T09:57:11+01:00")
        every { pcrTestResultDonorSettings.clear() } just Runs
        every { raTestResultDonorSettings.clear() } just Runs

        val lastCombinedResults = LastCombinedRiskResults(combinedResult, combinedResult)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns
            flowOf(lastCombinedResults)

        analyticsTestResultCollector = AnalyticsTestResultCollector(
            analyticsSettings,
            pcrTestResultDonorSettings,
            raTestResultDonorSettings,
            riskLevelStorage,
            timeStamper,
        )
    }

    @Test
    fun `saveTestResultAnalyticsSettings does not save anything when no user consent`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
            analyticsTestResultCollector.saveTestResult(PCR_POSITIVE, PCR)

            verify(exactly = 0) {
                pcrTestResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any())
                raTestResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any())
            }

            analyticsTestResultCollector.saveTestResult(RAT_POSITIVE, RAPID_ANTIGEN)

            verify(exactly = 0) {
                pcrTestResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any())
                raTestResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any())
            }
        }

    @Test
    fun `saveTestResultAnalyticsSettings saves data when user gave consent`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
            every { pcrTestResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any()) } just Runs
            every { raTestResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any()) } just Runs
            analyticsTestResultCollector.saveTestResult(PCR_POSITIVE, PCR)

            verify(exactly = 1) {
                pcrTestResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any())
            }

            analyticsTestResultCollector.saveTestResult(RAT_POSITIVE, RAPID_ANTIGEN)

            verify(exactly = 1) {
                raTestResultDonorSettings.saveTestResultDonorDataAtRegistration(any(), any())
            }
        }

    @Test
    fun `saveTestResultAnalyticsSettings does not save data when TestResult is INVALID`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
            analyticsTestResultCollector.saveTestResult(PCR_INVALID, PCR)
            analyticsTestResultCollector.saveTestResult(RAT_INVALID, RAPID_ANTIGEN)

            verify {
                analyticsSettings.analyticsEnabled wasNot Called
            }
        }

    @Test
    fun `saveTestResultAnalyticsSettings does not save data when TestResult is REDEEMED`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
            analyticsTestResultCollector.saveTestResult(PCR_REDEEMED, PCR)
            analyticsTestResultCollector.saveTestResult(RAT_REDEEMED, RAPID_ANTIGEN)
            verify {
                analyticsSettings.analyticsEnabled wasNot Called
            }
        }

    @Test
    fun `updatePendingTestResultReceivedTime doesn't update when TestResult isn't POS or NEG`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
            every { pcrTestResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
            every { pcrTestResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(
                PCR_OR_RAT_PENDING
            )
            for (testResult in listOf(PCR_REDEEMED, PCR_INVALID, PCR_OR_RAT_PENDING)) {
                analyticsTestResultCollector.updatePendingTestResultReceivedTime(testResult, PCR)

                verify {
                    analyticsSettings.analyticsEnabled
                    pcrTestResultDonorSettings.testScannedAfterConsent
                    pcrTestResultDonorSettings.testResultAtRegistration
                    pcrTestResultDonorSettings.finalTestResultReceivedAt wasNot Called
                    pcrTestResultDonorSettings.testResultAtRegistration wasNot Called
                }
            }

            every { raTestResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
            every { raTestResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(
                PCR_OR_RAT_PENDING
            )
            for (testResult in listOf(RAT_REDEEMED, RAT_INVALID, PCR_OR_RAT_PENDING)) {
                analyticsTestResultCollector.updatePendingTestResultReceivedTime(testResult, RAPID_ANTIGEN)

                verify {
                    analyticsSettings.analyticsEnabled
                    raTestResultDonorSettings.testScannedAfterConsent
                    raTestResultDonorSettings.testResultAtRegistration
                    raTestResultDonorSettings.finalTestResultReceivedAt wasNot Called
                    raTestResultDonorSettings.testResultAtRegistration wasNot Called
                }
            }
        }

    @Test
    fun `updatePendingTestResultReceivedTime doesn't update when Test is not scanned after consent`() =
        runBlockingTest {
            every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
            every { pcrTestResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(false)
            every { pcrTestResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(PCR_OR_RAT_PENDING)
            analyticsTestResultCollector.updatePendingTestResultReceivedTime(PCR_NEGATIVE, PCR)

            verify {
                analyticsSettings.analyticsEnabled
                pcrTestResultDonorSettings.testScannedAfterConsent
                pcrTestResultDonorSettings.testResultAtRegistration wasNot Called
                pcrTestResultDonorSettings.finalTestResultReceivedAt wasNot Called
                pcrTestResultDonorSettings.testResultAtRegistration wasNot Called
            }

            every { raTestResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(false)
            every { raTestResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(PCR_OR_RAT_PENDING)
            analyticsTestResultCollector.updatePendingTestResultReceivedTime(RAT_NEGATIVE, RAPID_ANTIGEN)

            verify {
                analyticsSettings.analyticsEnabled
                raTestResultDonorSettings.testScannedAfterConsent
                raTestResultDonorSettings.testResultAtRegistration wasNot Called
                raTestResultDonorSettings.finalTestResultReceivedAt wasNot Called
                raTestResultDonorSettings.testResultAtRegistration wasNot Called
            }
        }

    @Test
    fun `updatePendingTestResultReceivedTime update when TestResult is POS or NEG`() =
        runBlockingTest {
            for (testResult in listOf(PCR_NEGATIVE, PCR_POSITIVE)) {
                every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
                every { pcrTestResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
                every { pcrTestResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(
                    PCR_OR_RAT_PENDING
                )
                every { pcrTestResultDonorSettings.finalTestResultReceivedAt } returns mockFlowPreference(Instant.EPOCH)
                analyticsTestResultCollector.updatePendingTestResultReceivedTime(testResult, PCR)

                verify {
                    analyticsSettings.analyticsEnabled
                    pcrTestResultDonorSettings.testScannedAfterConsent
                    pcrTestResultDonorSettings.testResultAtRegistration
                    pcrTestResultDonorSettings.finalTestResultReceivedAt
                    pcrTestResultDonorSettings.testResultAtRegistration
                }
            }

            for (testResult in listOf(RAT_NEGATIVE, RAT_POSITIVE)) {
                every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
                every { raTestResultDonorSettings.testScannedAfterConsent } returns mockFlowPreference(true)
                every { raTestResultDonorSettings.testResultAtRegistration } returns mockFlowPreference(
                    PCR_OR_RAT_PENDING
                )
                every { raTestResultDonorSettings.finalTestResultReceivedAt } returns mockFlowPreference(Instant.EPOCH)
                analyticsTestResultCollector.updatePendingTestResultReceivedTime(testResult, RAPID_ANTIGEN)

                verify {
                    analyticsSettings.analyticsEnabled
                    pcrTestResultDonorSettings.testScannedAfterConsent
                    pcrTestResultDonorSettings.testResultAtRegistration
                    pcrTestResultDonorSettings.finalTestResultReceivedAt
                    pcrTestResultDonorSettings.testResultAtRegistration
                }
            }
        }

    @Test
    fun `clear is clearing saved data`() {
        analyticsTestResultCollector.clear(PCR)
        verify {
            pcrTestResultDonorSettings.clear()
        }
        analyticsTestResultCollector.clear(RAPID_ANTIGEN)
        verify {
            raTestResultDonorSettings.clear()
        }
    }
}
