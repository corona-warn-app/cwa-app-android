package de.rki.coronawarnapp.risk.changedetection

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.LastCombinedRiskResults
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.RiskState.CALCULATION_FAILED
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class CombinedRiskLevelChangeDetectorTest : BaseTest() {
    @MockK lateinit var context: Context
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var notificationManagerCompat: NotificationManagerCompat
    @MockK lateinit var riskLevelSettings: RiskLevelSettings
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var tracingSettings: TracingSettings
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

        every { tracingSettings.isUserToBeNotifiedOfLoweredRiskLevel } returns mockFlowPreference(false)
        every { tracingSettings.showRiskLevelBadge } returns mockFlowPreference(false)
        every { tracingSettings.isUserToBeNotifiedOfAdditionalHighRiskLevel } returns mockFlowPreference(false)
        every { tracingSettings.lastHighRiskDate = any() } just Runs
        every { tracingSettings.lastHighRiskDate } returns null
        every { coronaTestRepository.coronaTests } returns coronaTests
        every { notificationManagerCompat.areNotificationsEnabled() } returns true

        every { riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp = any() } just Runs
        every { riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp } returns null

        every { builder.build() } returns notification
        every { builder.setContentTitle(any()) } returns builder
        every { builder.setContentTextExpandable(any()) } returns builder
        every { builder.setContentText(any()) } returns builder
        every { builder.setStyle(any()) } returns builder
        every { notificationHelper.newBaseBuilder() } returns builder
        every { notificationHelper.sendNotification(any(), any()) } just Runs
        every { context.getString(any()) } returns ""
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
        riskState = riskState
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
        lastSuccessfullyCalculated: CombinedEwPtRiskLevelResult,
        lastRiskEncounterAt: LocalDate
    ): LastCombinedRiskResults {

        val lastSuccessfullyCalculatedSpy = spyk(lastSuccessfullyCalculated)
        every { lastSuccessfullyCalculatedSpy.lastRiskEncounterAt } returns lastRiskEncounterAt

        return LastCombinedRiskResults(
            lastCalculated = lastSuccessfullyCalculated,
            lastSuccessfullyCalculated = lastSuccessfullyCalculatedSpy
        )
    }

    private fun createInstance(scope: CoroutineScope) = CombinedRiskLevelChangeDetector(
        context = context,
        appScope = scope,
        riskLevelStorage = riskLevelStorage,
        notificationManagerCompat = notificationManagerCompat,
        riskLevelSettings = riskLevelSettings,
        notificationHelper = notificationHelper,
        coronaTestRepository = coronaTestRepository,
        tracingSettings = tracingSettings
    )

    @Test
    fun `no notification should be sent if there is only one result yet`() {

        val riskSequence = listOf(createCombinedRiskLevel(LOW_RISK))

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)

        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns
            flowOf(createLastCombinedRiskResults(riskSequence.first(), LocalDate.parse("2022-01-01")))

        runBlockingTest {
            createInstance(scope = this).launch()

            advanceUntilIdle()

            coVerifySequence {
                notificationHelper wasNot Called
            }

            verify(exactly = 0) {
                tracingSettings.isUserToBeNotifiedOfAdditionalHighRiskLevel.update(any())
                tracingSettings.isUserToBeNotifiedOfLoweredRiskLevel.update(any())
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
            *riskSequence.map { createLastCombinedRiskResults(it, it.calculatedAt.toLocalDateUtc()) }.toTypedArray()
        )

        runBlockingTest {
            createInstance(scope = this).launch()

            advanceUntilIdle()

            coVerifySequence {
                notificationManagerCompat wasNot Called
            }
        }

        verify(exactly = 0) {
            tracingSettings.isUserToBeNotifiedOfAdditionalHighRiskLevel.update(any())
            tracingSettings.isUserToBeNotifiedOfLoweredRiskLevel.update(any())
        }
    }

    @Test
    fun `combined risk state change from HIGH to LOW should trigger one notification`() {

        val riskSequence = listOf(
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-03")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-02")),
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-01"))
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequence
                .sortedBy { it.calculatedAt }
                .map { createLastCombinedRiskResults(it, it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        runBlockingTest {
            createInstance(scope = this).launch()

            advanceUntilIdle()

            verify(exactly = 1) { tracingSettings.isUserToBeNotifiedOfLoweredRiskLevel }
            verify(exactly = 1) { notificationHelper.sendNotification(any(), any()) }
            verify(exactly = 1) { tracingSettings.showRiskLevelBadge }
        }
    }

    @Test
    fun `combined risk level went from LOW to HIGH but it is has already been processed`() {
        val riskSequence = listOf(
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-03")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-02")),
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-01"))
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequence
                .sortedBy { it.calculatedAt }
                .map { createLastCombinedRiskResults(it, it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        every { riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp } returns Instant.parse("2022-01-03")

        runBlockingTest {
            createInstance(scope = this).launch()

            advanceUntilIdle()

            coVerifySequence {
                notificationHelper wasNot Called
                tracingSettings.showRiskLevelBadge wasNot Called
            }
        }
    }

    @Test
    fun `combined risk state change from LOW to HIGH triggers initial high notification`() {
        val riskSequence = listOf(
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-04")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-03")),
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-02")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-01")),
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequence
                .sortedBy { it.calculatedAt }
                .map { createLastCombinedRiskResults(it, it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        runBlockingTest {
            createInstance(scope = this).launch()

            advanceUntilIdle()

            verify(exactly = 1) { notificationHelper.sendNotification(any(), any()) }
            verify(exactly = 1) { tracingSettings.showRiskLevelBadge }
        }
    }

    @Test
    fun `multiple high risks should trigger initial notification and additional high risk notifications`() {

        TODO("Fix tests - problems, because tracingSettings.lastHighRiskDate is mocked - capture argument?")
        TODO("Reverse flow emissions in tests above?")

        val riskSequence = listOf(
            createCombinedRiskLevel(
                INCREASED_RISK,
                calculatedAt = Instant.parse("2022-01-01")
            ), // calculatedAt before last notification was sent - no notification
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-06")),
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-05")),
            createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.parse("2022-01-04")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-03")),
            createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.parse("2022-01-02")),
            createCombinedRiskLevel(CALCULATION_FAILED, calculatedAt = Instant.parse("2022-01-01")),
        )

        every { riskLevelStorage.allCombinedEwPtRiskLevelResults } returns flowOf(riskSequence)
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            *riskSequence
                .sortedBy { it.calculatedAt }
                .map { createLastCombinedRiskResults(it, it.calculatedAt.toLocalDateUtc()) }
                .toTypedArray()
        )

        runBlockingTest {
            createInstance(scope = this).launch()

            advanceUntilIdle()

            verify(exactly = 3) { notificationHelper.sendNotification(any(), any()) }
            verify(exactly = 2) { tracingSettings.isUserToBeNotifiedOfAdditionalHighRiskLevel.update(any()) }
            verify(exactly = 1) { tracingSettings.showRiskLevelBadge }
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
