package de.rki.coronawarnapp.risk.changedetection

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.minusDaysAtStartOfDayUtc
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.LastCombinedRiskResults
import de.rki.coronawarnapp.risk.RiskCardDisplayInfo
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.RiskState.CALCULATION_FAILED
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import de.rki.coronawarnapp.util.toLocalDateUtc
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore
import java.time.Instant
import java.time.LocalDate

@Suppress("MaxLineLength")
class CombinedRiskLevelChangeDetectorTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var notificationManagerCompat: NotificationManagerCompat
    @MockK lateinit var riskLevelSettings: RiskLevelSettings
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var builder: NotificationCompat.Builder
    @MockK lateinit var notification: Notification
    @MockK lateinit var riskCardDisplayInfo: RiskCardDisplayInfo

    private val dataStore = FakeDataStore()
    private val tracingSettings = TracingSettings(dataStore)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { notificationManagerCompat.areNotificationsEnabled() } returns true

        coEvery { riskLevelSettings.updateLastChangeCheckedRiskLevelCombinedTimestamp(any()) } just Runs
        every { riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp } returns flowOf(null)

        every { builder.build() } returns notification
        every { builder.setContentTitle(any()) } returns builder
        every { builder.setContentTextExpandable(any()) } returns builder
        every { builder.setContentText(any()) } returns builder
        every { builder.setStyle(any()) } returns builder
        every { notificationHelper.newBaseBuilder() } returns builder
        every { notificationHelper.sendNotification(any(), any()) } just Runs
        every { context.getString(any()) } returns ""
        coEvery { riskCardDisplayInfo.shouldShowRiskCard(any()) } returns true
    }

    @AfterEach
    fun cleanup() {
        dataStore.reset()
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

    private fun createLastCombinedRiskResults(
        lastCalculatedResult: CombinedEwPtRiskLevelResult,
        lastRiskEncounterAt: LocalDate
    ): LastCombinedRiskResults {

        val lastCalculatedResultSpy = spyk(lastCalculatedResult)
        every { lastCalculatedResultSpy.lastRiskEncounterAt } returns lastRiskEncounterAt

        return LastCombinedRiskResults(
            lastCalculated = lastCalculatedResultSpy,
            lastSuccessfullyCalculatedRiskState = LOW_RISK
        )
    }

    private fun createInstance(scope: CoroutineScope) = CombinedRiskLevelChangeDetector(
        context = context,
        appScope = scope,
        riskLevelStorage = riskLevelStorage,
        notificationManagerCompat = notificationManagerCompat,
        riskLevelSettings = riskLevelSettings,
        notificationHelper = notificationHelper,
        tracingSettings = tracingSettings,
        riskCardDisplayInfo = riskCardDisplayInfo
    )

    @Test
    fun `no notification should be sent if there is only one result yet`() {

        val riskSequence = listOf(createCombinedRiskLevel(LOW_RISK))

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)

        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns
            flowOf(createLastCombinedRiskResults(riskSequence.first(), LocalDate.parse("2022-01-01")))

        runTest {
            createInstance(scope = this).initialize()

            advanceUntilIdle()

            verify {
                notificationHelper wasNot Called
            }

            with(tracingSettings) {
                isUserToBeNotifiedOfAdditionalHighRiskLevel.first() shouldBe false
                isUserToBeNotifiedOfLoweredRiskLevel.first() shouldBe false
            }
        }
    }

    @Test
    fun `no risk level change, nothing should happen`() {

        val riskSequence = listOf(
            createCombinedRiskLevel(LOW_RISK),
            createCombinedRiskLevel(CALCULATION_FAILED),
            createCombinedRiskLevel(LOW_RISK)
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequence
                .sortedBy { it.calculatedAt }
                .map { createLastCombinedRiskResults(it, it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        runTest {
            createInstance(scope = this).initialize()

            advanceUntilIdle()

            verify {
                notificationManagerCompat wasNot Called
            }

            with(tracingSettings) {
                isUserToBeNotifiedOfAdditionalHighRiskLevel.first() shouldBe false
                isUserToBeNotifiedOfLoweredRiskLevel.first() shouldBe false
            }
        }
    }

    @Test
    fun `combined risk state change from HIGH to LOW should trigger one notification`() {

        val riskSequence = listOf(
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-03T12:00:00.000Z")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-02T12:00:00.000Z")),
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-01T12:00:00.000Z"))
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequence
                .sortedBy { it.calculatedAt }
                .map { createLastCombinedRiskResults(it, it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        runTest {
            createInstance(scope = this).initialize()

            advanceUntilIdle()

            with(tracingSettings) {
                isUserToBeNotifiedOfLoweredRiskLevel.first() shouldBe true
                showRiskLevelBadge.first() shouldBe true
            }

            verify(exactly = 1) {
                notificationHelper.sendNotification(any(), any())
            }
        }
    }

    @Test
    fun `combined risk state change from HIGH to LOW should NOT trigger notification if low risk card is NOT displayed`() {

        coEvery { riskCardDisplayInfo.shouldShowRiskCard(LOW_RISK) } returns false

        val riskSequence = listOf(
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-03T12:00:00.000Z")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-02T12:00:00.000Z")),
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-01T12:00:00.000Z"))
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequence
                .sortedBy { it.calculatedAt }
                .map { createLastCombinedRiskResults(it, it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        runTest {
            createInstance(scope = this).initialize()

            advanceUntilIdle()

            with(tracingSettings) {
                isUserToBeNotifiedOfLoweredRiskLevel.first() shouldBe false
                showRiskLevelBadge.first() shouldBe false
            }

            verify(exactly = 0) {
                notificationHelper.sendNotification(any(), any())
            }
        }
    }

    @Test
    fun `combined risk level went from LOW to HIGH but it is has already been processed`() {
        val riskSequence = listOf(
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-03T12:00:00.000Z")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-02T12:00:00.000Z")),
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-01T12:00:00.000Z"))
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequence
                .sortedBy { it.calculatedAt }
                .map { createLastCombinedRiskResults(it, it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        every { riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp } returns flowOf(Instant.parse("2022-01-03T12:00:00.000Z"))

        runTest {
            createInstance(scope = this).initialize()

            advanceUntilIdle()

            tracingSettings.showRiskLevelBadge.first() shouldBe false

            verify {
                notificationHelper wasNot Called
            }
        }
    }

    @Test
    fun `combined risk state change from LOW to HIGH triggers initial high notification`() {
        val riskSequence = listOf(
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-04T12:00:00.000Z")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-03T12:00:00.000Z")),
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-02T12:00:00.000Z")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-01T12:00:00.000Z")),
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequence
                .sortedBy { it.calculatedAt }
                .map { createLastCombinedRiskResults(it, it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        runTest {
            createInstance(scope = this).initialize()

            advanceUntilIdle()

            with(tracingSettings) {
                showRiskLevelBadge.first() shouldBe true
            }

            verify(exactly = 1) {
                notificationHelper.sendNotification(any(), any())
            }
        }
    }

    @Test
    fun `multiple high risks should trigger initial notification and additional high risk notifications`() {

        val riskSequenceWithLowToHighRiskChange = listOf(
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-04T12:00:00.000Z")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-03T12:00:00.000Z")),
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-02T12:00:00.000Z")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-01T12:00:00.000Z")),
        )

        val riskSequenceWithAdditionalRisk = riskSequenceWithLowToHighRiskChange + listOf(
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-06T12:00:00.000Z")),
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-05T12:00:00.000Z")),
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequenceWithLowToHighRiskChange)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequenceWithAdditionalRisk
                .sortedBy { it.calculatedAt }
                .filter { it.wasSuccessfullyCalculated }
                .map { createLastCombinedRiskResults(it, it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        runTest {
            createInstance(scope = this).initialize()

            advanceUntilIdle()

            // 3 notifications in total ...
            verify(exactly = 3) { notificationHelper.sendNotification(any(), any()) }

            with(tracingSettings) {
                showRiskLevelBadge.first() shouldBe true
                isUserToBeNotifiedOfAdditionalHighRiskLevel.first() shouldBe true
            }
        }
    }

    @Test
    fun `lastHighRiskDate should be reset when there is a new low risk after a high risk`() {
        val riskSequence = listOf(
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-05T12:00:00.000Z")),
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-06T12:00:00.000Z")),
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequence
                .map { createLastCombinedRiskResults(it, lastRiskEncounterAt = it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        runTest {
            createInstance(scope = this).initialize()

            advanceUntilIdle()

            tracingSettings.lastHighRiskDate.first() shouldBe null
        }
    }

    @Test
    fun `lastHighRiskDate should NOT be reset when there is a new low risk before a high risk`() {
        val riskSequence = listOf(
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-02T12:00:00.000Z")),
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-03T12:00:00.000Z")),
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-01T12:00:00.000Z")),
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequence
                .map { createLastCombinedRiskResults(it, lastRiskEncounterAt = it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        runTest {
            createInstance(scope = this).initialize()

            advanceUntilIdle()

            tracingSettings.lastHighRiskDate.first() shouldNotBe null
        }
    }

    @Test
    fun `evaluate risk level change detection function low to high`() {
        CALCULATION_FAILED.hasChangedFromLowToHigh(CALCULATION_FAILED) shouldBe false
        LOW_RISK.hasChangedFromLowToHigh(LOW_RISK) shouldBe false
        INCREASED_RISK.hasChangedFromLowToHigh(INCREASED_RISK) shouldBe false
        INCREASED_RISK.hasChangedFromLowToHigh(LOW_RISK) shouldBe false
        LOW_RISK.hasChangedFromLowToHigh(INCREASED_RISK) shouldBe true
        CALCULATION_FAILED.hasChangedFromLowToHigh(INCREASED_RISK) shouldBe false
        INCREASED_RISK.hasChangedFromLowToHigh(CALCULATION_FAILED) shouldBe false
    }

    @Test
    fun `evaluate risk level change detection function high to low`() {
        CALCULATION_FAILED.hasChangedFromHighToLow(CALCULATION_FAILED) shouldBe false
        LOW_RISK.hasChangedFromHighToLow(LOW_RISK) shouldBe false
        INCREASED_RISK.hasChangedFromHighToLow(INCREASED_RISK) shouldBe false
        INCREASED_RISK.hasChangedFromHighToLow(LOW_RISK) shouldBe true
        LOW_RISK.hasChangedFromHighToLow(INCREASED_RISK) shouldBe false
        CALCULATION_FAILED.hasChangedFromHighToLow(INCREASED_RISK) shouldBe false
        INCREASED_RISK.hasChangedFromHighToLow(CALCULATION_FAILED) shouldBe false
    }
}
