package de.rki.coronawarnapp.risk.changedetection

import android.app.Notification
import androidx.core.app.NotificationCompat
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PtRiskLevelChangeDetectorTest : BaseTest() {
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var riskLevelSettings: RiskLevelSettings
    @MockK lateinit var coronaTestRepository: CoronaTestRepository

    @MockK lateinit var builder: NotificationCompat.Builder
    @MockK lateinit var notification: Notification

    private val coronaTests: MutableStateFlow<Set<CoronaTest>> = MutableStateFlow(
        setOf(
            mockk<CoronaTest>().apply { every { isSubmitted } returns false }
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { coronaTestRepository.coronaTests } returns coronaTests

        every { riskLevelSettings.ptLastChangeToHighRiskLevelTimestamp = any() } just Runs
        every { riskLevelSettings.ptLastChangeToHighRiskLevelTimestamp } returns null
    }

    private fun createPtRiskLevel(
        riskState: RiskState,
        calculatedAt: Instant = Instant.EPOCH,
        presenceTracingDayRisk: List<PresenceTracingDayRisk>? = null
    ): PtRiskLevelResult = PtRiskLevelResult(
        calculatedAt = calculatedAt,
        riskState = riskState,
        presenceTracingDayRisk = presenceTracingDayRisk
    )

    private fun createInstance(scope: CoroutineScope) = PtRiskLevelChangeDetector(
        appScope = scope,
        riskLevelStorage = riskLevelStorage,
        riskLevelSettings = riskLevelSettings,
    )

    @Test
    fun `nothing happens if there is only one result yet`() {
        every { riskLevelStorage.allPtRiskLevelResults } returns flowOf(listOf(createPtRiskLevel(LOW_RISK)))

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerify(exactly = 0) {
                riskLevelSettings.ptLastChangeToHighRiskLevelTimestamp
            }
        }
    }

    @Test
    fun `no risk level change, nothing should happen`() {
        every { riskLevelStorage.allPtRiskLevelResults } returns flowOf(
            listOf(
                createPtRiskLevel(LOW_RISK),
                createPtRiskLevel(LOW_RISK)
            )
        )

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerify(exactly = 0) {
                riskLevelSettings.ptLastChangeToHighRiskLevelTimestamp
            }
        }
    }

    @Test
    fun `risk level went from LOW to HIGH`() {
        val dayRisk = PresenceTracingDayRisk(
            Instant.EPOCH.plus(1).toLocalDateUtc(),
            INCREASED_RISK,
        )
        every { riskLevelStorage.allPtRiskLevelResults } returns flowOf(
            listOf(
                createPtRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH.plus(1), listOf(dayRisk)),
                createPtRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
            )
        )

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerify(exactly = 1) {
                riskLevelSettings.ptLastChangeToHighRiskLevelTimestamp =
                    Instant.EPOCH.plus(1)
            }
        }
    }

    @Test
    fun `risk level went from HIGH to LOW`() {
        val dayRisk = PresenceTracingDayRisk(
            Instant.EPOCH.plus(1).toLocalDateUtc(),
            LOW_RISK,
        )

        every { riskLevelStorage.allPtRiskLevelResults } returns flowOf(
            listOf(
                createPtRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH.plus(1), listOf(dayRisk)),
                createPtRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH)
            )
        )

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerify(exactly = 0) {
                riskLevelSettings.ptLastChangeToHighRiskLevelTimestamp
            }
        }
    }
}
