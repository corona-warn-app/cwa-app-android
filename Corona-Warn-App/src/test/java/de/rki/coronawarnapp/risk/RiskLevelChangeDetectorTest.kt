package de.rki.coronawarnapp.risk

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.risk.RiskState.CALCULATION_FAILED
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.device.ForegroundState
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
    @MockK lateinit var riskLevelSettings: RiskLevelSettings
    @MockK lateinit var notificationHelper: NotificationHelper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)

        every { LocalData.isUserToBeNotifiedOfLoweredRiskLevel = any() } just Runs
        every { LocalData.submissionWasSuccessful() } returns false
        every { foregroundState.isInForeground } returns flowOf(true)
        every { notificationManagerCompat.areNotificationsEnabled() } returns true
        every { riskLevelSettings.lastChangeCheckedRiskLevelTimestamp = any() } just Runs
        every { riskLevelSettings.lastChangeCheckedRiskLevelTimestamp } returns null
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createRiskLevel(
        riskState: RiskState,
        calculatedAt: Instant = Instant.EPOCH
    ): RiskLevelResult = object : RiskLevelResult {
        override val riskState: RiskState = riskState
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
        foregroundState = foregroundState,
        riskLevelSettings = riskLevelSettings,
        notificationHelper = notificationHelper
    )

    @Test
    fun `nothing happens if there is only one result yet`() {
        every { riskLevelStorage.latestRiskLevelResults } returns flowOf(listOf(createRiskLevel(LOW_RISK)))

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
        every { riskLevelStorage.latestRiskLevelResults } returns flowOf(
            listOf(
                createRiskLevel(LOW_RISK),
                createRiskLevel(LOW_RISK)
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
        every { riskLevelStorage.latestRiskLevelResults } returns flowOf(
            listOf(
                createRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH.plus(1)),
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
        every { riskLevelStorage.latestRiskLevelResults } returns flowOf(
            listOf(
                createRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH.plus(1)),
                createRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
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
    fun `risklevel went from LOW to HIGH but it is has already been processed`() {
        every { riskLevelStorage.latestRiskLevelResults } returns flowOf(
            listOf(
                createRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH.plus(1)),
                createRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
            )
        )
        every { riskLevelSettings.lastChangeCheckedRiskLevelTimestamp } returns Instant.EPOCH.plus(1)

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
    fun `evaluate risk level change detection function`() {
        RiskLevelChangeDetector.hasHighLowLevelChanged(CALCULATION_FAILED, CALCULATION_FAILED) shouldBe false
        RiskLevelChangeDetector.hasHighLowLevelChanged(LOW_RISK, LOW_RISK) shouldBe false
        RiskLevelChangeDetector.hasHighLowLevelChanged(INCREASED_RISK, INCREASED_RISK) shouldBe false
        RiskLevelChangeDetector.hasHighLowLevelChanged(INCREASED_RISK, LOW_RISK) shouldBe true
        RiskLevelChangeDetector.hasHighLowLevelChanged(LOW_RISK, INCREASED_RISK) shouldBe true
        RiskLevelChangeDetector.hasHighLowLevelChanged(CALCULATION_FAILED, INCREASED_RISK) shouldBe true
        RiskLevelChangeDetector.hasHighLowLevelChanged(INCREASED_RISK, CALCULATION_FAILED) shouldBe true
    }
}
