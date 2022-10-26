package de.rki.coronawarnapp.risk.changedetection

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.minusDaysAtStartOfDayUtc
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import testhelpers.preferences.FakeDataStore

class EwRiskLevelChangeDetectorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var surveys: Surveys
    lateinit var riskLevelSettings: RiskLevelSettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        riskLevelSettings = RiskLevelSettings(FakeDataStore())
        coEvery { surveys.resetSurvey(Surveys.Type.HIGH_RISK_ENCOUNTER) } just Runs
    }

    private fun createEwRiskLevel(
        riskState: RiskState,
        calculatedAt: Instant = Instant.EPOCH,
        ewAggregatedRiskResult: EwAggregatedRiskResult? = null
    ): EwRiskLevelResult = object : EwRiskLevelResult {
        override val riskState: RiskState = riskState
        override val calculatedAt: Instant = calculatedAt
        override val ewAggregatedRiskResult: EwAggregatedRiskResult? = ewAggregatedRiskResult
        override val failureReason: EwRiskLevelResult.FailureReason? = null
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
    }

    private fun createPtRiskLevel(
        riskState: RiskState,
        calculatedAt: Instant = Instant.EPOCH
    ): PtRiskLevelResult = PtRiskLevelResult(
        calculatedAt = calculatedAt,
        riskState = riskState,
        calculatedFrom = calculatedAt.minusDaysAtStartOfDayUtc(10).toInstant()
    )

    private fun createCombinedRiskLevel(
        riskState: RiskState,
        calculatedAt: Instant = Instant.EPOCH,
        ewAggregatedRiskResult: EwAggregatedRiskResult? = null
    ): CombinedEwPtRiskLevelResult = CombinedEwPtRiskLevelResult(
        ewRiskLevelResult = createEwRiskLevel(riskState, calculatedAt, ewAggregatedRiskResult),
        ptRiskLevelResult = createPtRiskLevel(riskState, calculatedAt)
    )

    private fun createInstance(scope: CoroutineScope) = EwRiskLevelChangeDetector(
        appScope = scope,
        riskLevelStorage = riskLevelStorage,
        riskLevelSettings = riskLevelSettings,
        surveys = surveys,
    )

    @Test
    fun `nothing happens if there is only one result yet`() {
        every { riskLevelStorage.allEwRiskLevelResults } returns flowOf(listOf(createEwRiskLevel(LOW_RISK)))

        runTest {
            val instance = createInstance(scope = this)
            instance.initialize()

            advanceUntilIdle()

            coVerifySequence {
                surveys wasNot Called
            }
        }
    }

    @Test
    fun `no risk level change, nothing should happen`() {
        every { riskLevelStorage.allEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(LOW_RISK),
                createEwRiskLevel(LOW_RISK)
            )
        )

        runTest {
            val instance = createInstance(scope = this)
            instance.initialize()

            advanceUntilIdle()

            coVerifySequence {
                surveys wasNot Called
            }
        }
    }

    @Test
    fun `risk level went from HIGH to LOW resets survey`() {
        every { riskLevelStorage.allEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH.plusMillis(1)),
                createEwRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH)
            )
        )

        runTest {
            val instance = createInstance(scope = this)
            instance.initialize()

            advanceUntilIdle()

            coVerifySequence {
                surveys.resetSurvey(Surveys.Type.HIGH_RISK_ENCOUNTER)
            }
        }
    }

    @Test
    fun `risk level went from LOW to HIGH`() {
        every { riskLevelStorage.allEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH.plusMillis(1)),
                createEwRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
            )
        )

        runTest {
            val instance = createInstance(scope = this)
            instance.initialize()

            advanceUntilIdle()

            coVerifySequence {
                surveys wasNot Called
            }
        }
    }

    @Test
    fun `risk level went from LOW to HIGH but it is has already been processed`() {
        every { riskLevelStorage.allEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH.plusMillis(1)),
                createEwRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
            )
        )

        runTest {
            val instance = createInstance(scope = this)
            instance.initialize()

            advanceUntilIdle()

            coVerifySequence {
                surveys wasNot Called
            }
        }
    }

    @Test
    fun `combined risk level went from LOW to HIGH but it is has already been processed`() {
        every { riskLevelStorage.allEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(LOW_RISK)
            )
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns
            flowOf(
                listOf(
                    createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH.plusMillis(1)),
                    createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
                )
            )

        runTest {
            val instance = createInstance(scope = this)
            instance.initialize()

            advanceUntilIdle()

            coVerifySequence {
                surveys wasNot Called
            }
        }
    }

    @Test
    fun `mostRecentDateWithHighOrLowRiskLevel is updated every time`() {
        every { riskLevelStorage.allEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(
                    INCREASED_RISK,
                    calculatedAt = Instant.EPOCH.plusMillis(1),
                    ewAggregatedRiskResult = mockk<EwAggregatedRiskResult>().apply {
                        every { mostRecentDateWithHighRisk } returns Instant.EPOCH.plusMillis(10)
                        every { isIncreasedRisk() } returns true
                    }
                ),
                createEwRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
            )
        )
        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns
            flowOf(listOf(createCombinedRiskLevel(LOW_RISK)))

        runTest {
            val instance = createInstance(scope = this)
            instance.initialize()
            advanceUntilIdle()
        }

        every { riskLevelStorage.allEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(
                    INCREASED_RISK,
                    calculatedAt = Instant.EPOCH.plusMillis(1),
                    ewAggregatedRiskResult = mockk<EwAggregatedRiskResult>().apply {
                        every { mostRecentDateWithLowRisk } returns Instant.EPOCH.plusMillis(20)
                        every { isIncreasedRisk() } returns false
                    }
                ),
                createEwRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
            )
        )

        runTest {
            val instance = createInstance(scope = this)
            instance.initialize()
            advanceUntilIdle()
        }
    }
}
