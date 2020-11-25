package de.rki.coronawarnapp.risk

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.RiskLevel.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskLevel.LOW_LEVEL_RISK
import de.rki.coronawarnapp.risk.RiskLevel.UNDETERMINED
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.ForegroundState
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RiskLevelChangeDetectorTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var notificationManagerCompat: NotificationManagerCompat
    @MockK lateinit var foregroundState: ForegroundState

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)

        every { LocalData.isUserToBeNotifiedOfLoweredRiskLevel = any() } just Runs
        every { LocalData.submissionWasSuccessful() } returns false
        every { foregroundState.isInForeground } returns flowOf(true)
        every { notificationManagerCompat.areNotificationsEnabled() } returns true
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createRiskLevel(
        riskLevel: RiskLevel,
        calculatedAt: Instant = Instant.EPOCH
    ): RiskLevelResult = object : RiskLevelResult {
        override val riskLevel: RiskLevel = riskLevel
        override val calculatedAt: Instant = calculatedAt
        override val aggregatedRiskResult: AggregatedRiskResult? = null
        override val failureReason: RiskLevelResult.FailureReason? = null
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
        override val daysWithEncounters: Int = 0
    }

    private fun createInstance(scope: CoroutineScope) = RiskLevelChangeDetector(
        context = context,
        appScope = scope,
        riskLevelStorage = riskLevelStorage,
        notificationManagerCompat = notificationManagerCompat,
        foregroundState = foregroundState
    )

    @Test
    fun `nothing happens if there is only one result yet`() {
        every { riskLevelStorage.riskLevelResults } returns flowOf(listOf(createRiskLevel(LOW_LEVEL_RISK)))

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                LocalData wasNot Called
                notificationManagerCompat wasNot Called
            }
        }
    }

    @Test
    fun `no risklevel change, nothing should happen`() {
        every { riskLevelStorage.riskLevelResults } returns flowOf(
            listOf(
                createRiskLevel(LOW_LEVEL_RISK),
                createRiskLevel(LOW_LEVEL_RISK)
            )
        )

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                LocalData wasNot Called
                notificationManagerCompat wasNot Called
            }
        }
    }

    @Test
    fun `risklevel went from HIGH to LOW`() {
        every { riskLevelStorage.riskLevelResults } returns flowOf(
            listOf(
                createRiskLevel(LOW_LEVEL_RISK, calculatedAt = Instant.EPOCH.plus(1)),
                createRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH)
            )
        )

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                LocalData.submissionWasSuccessful()
                foregroundState.isInForeground
                LocalData.isUserToBeNotifiedOfLoweredRiskLevel = any()
            }
        }
    }

    @Test
    fun `risklevel went from LOW to HIGH`() {
        every { riskLevelStorage.riskLevelResults } returns flowOf(
            listOf(
                createRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH.plus(1)),
                createRiskLevel(LOW_LEVEL_RISK, calculatedAt = Instant.EPOCH)
            )
        )

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                LocalData.submissionWasSuccessful()
                foregroundState.isInForeground
            }
        }
    }

    @Test
    fun `evaluate risk level change detection function`() {
        RiskLevelChangeDetector.hasHighLowLevelChanged(UNDETERMINED, UNDETERMINED) shouldBe false
        RiskLevelChangeDetector.hasHighLowLevelChanged(LOW_LEVEL_RISK, LOW_LEVEL_RISK) shouldBe false
        RiskLevelChangeDetector.hasHighLowLevelChanged(INCREASED_RISK, INCREASED_RISK) shouldBe false
        RiskLevelChangeDetector.hasHighLowLevelChanged(INCREASED_RISK, LOW_LEVEL_RISK) shouldBe true
        RiskLevelChangeDetector.hasHighLowLevelChanged(LOW_LEVEL_RISK, INCREASED_RISK) shouldBe true
        RiskLevelChangeDetector.hasHighLowLevelChanged(UNDETERMINED, INCREASED_RISK) shouldBe true
        RiskLevelChangeDetector.hasHighLowLevelChanged(INCREASED_RISK, UNDETERMINED) shouldBe true
    }
}
