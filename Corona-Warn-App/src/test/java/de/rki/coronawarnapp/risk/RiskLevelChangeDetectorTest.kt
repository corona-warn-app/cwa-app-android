package de.rki.coronawarnapp.risk

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState.CALCULATION_FAILED
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import io.kotest.matchers.shouldBe
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class RiskLevelChangeDetectorTest : BaseTest() {
    @MockK lateinit var context: Context
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var notificationManagerCompat: NotificationManagerCompat
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var riskLevelSettings: RiskLevelSettings
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var surveys: Surveys
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var tracingSettings: TracingSettings
    @MockK lateinit var testResultDonorSettings: TestResultDonorSettings

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
        every { coronaTestRepository.coronaTests } returns coronaTests
        every { foregroundState.isInForeground } returns flowOf(false)
        every { notificationManagerCompat.areNotificationsEnabled() } returns true

        every { riskLevelSettings.lastChangeCheckedRiskLevelTimestamp = any() } just Runs
        every { riskLevelSettings.lastChangeCheckedRiskLevelTimestamp } returns null

        every { riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp = any() } just Runs
        every { riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp } returns null

        every { riskLevelSettings.lastChangeToHighRiskLevelTimestamp = any() } just Runs
        every { riskLevelSettings.lastChangeToHighRiskLevelTimestamp } returns null

        coEvery { surveys.resetSurvey(Surveys.Type.HIGH_RISK_ENCOUNTER) } just Runs

        every { testResultDonorSettings.riskLevelTurnedRedTime } returns mockFlowPreference(null)
        every { testResultDonorSettings.mostRecentDateWithHighOrLowRiskLevel } returns mockFlowPreference(null)

        every { builder.build() } returns notification
        every { builder.setContentTitle(any()) } returns builder
        every { builder.setContentTextExpandable(any()) } returns builder
        every { builder.setContentText(any()) } returns builder
        every { builder.setStyle(any()) } returns builder
        every { notificationHelper.newBaseBuilder() } returns builder
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
        override val daysWithEncounters: Int = 0
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

    private fun createInstance(scope: CoroutineScope) = RiskLevelChangeDetector(
        context = context,
        appScope = scope,
        riskLevelStorage = riskLevelStorage,
        notificationManagerCompat = notificationManagerCompat,
        foregroundState = foregroundState,
        riskLevelSettings = riskLevelSettings,
        notificationHelper = notificationHelper,
        surveys = surveys,
        coronaTestRepository = coronaTestRepository,
        tracingSettings = tracingSettings,
        testResultDonorSettings = testResultDonorSettings
    )

    @Test
    fun `nothing happens if there is only one result yet`() {
        every { riskLevelStorage.latestCombinedEwPtRiskLevelResults } returns
            flowOf(listOf(createCombinedRiskLevel(LOW_RISK)))
        every { riskLevelStorage.latestEwRiskLevelResults } returns flowOf(listOf(createEwRiskLevel(LOW_RISK)))

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                notificationManagerCompat wasNot Called
                surveys wasNot Called
                testResultDonorSettings wasNot Called
            }
        }
    }

    @Test
    fun `no risk level change, nothing should happen`() {
        every { riskLevelStorage.latestEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(LOW_RISK),
                createEwRiskLevel(LOW_RISK)
            )
        )

        every { riskLevelStorage.latestCombinedEwPtRiskLevelResults } returns
            flowOf(
                listOf(
                    createCombinedRiskLevel(LOW_RISK),
                    createCombinedRiskLevel(LOW_RISK)
                )
            )

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                notificationManagerCompat wasNot Called
                surveys wasNot Called
            }
        }
    }

    @Test
    fun `combined risk state change from HIGH to LOW triggers notification`() {
        every { riskLevelStorage.latestEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(LOW_RISK)
            )
        )

        every { riskLevelStorage.latestCombinedEwPtRiskLevelResults } returns
            flowOf(
                listOf(
                    createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH.plus(1)),
                    createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH)
                )
            )

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                coronaTestRepository.coronaTests
                foregroundState.isInForeground
                notificationHelper.newBaseBuilder()
                notificationHelper.sendNotification(any(), any())
            }
        }
    }

    @Test
    fun `combined risk state change from LOW to HIGH triggers notification`() {
        every { riskLevelStorage.latestEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(LOW_RISK)
            )
        )

        every { riskLevelStorage.latestCombinedEwPtRiskLevelResults } returns
            flowOf(
                listOf(
                    createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH.plus(1)),
                    createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
                )
            )

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                coronaTestRepository.coronaTests
                foregroundState.isInForeground
                notificationHelper.newBaseBuilder()
                notificationHelper.sendNotification(any(), any())
            }
        }
    }

    @Test
    fun `risk level went from HIGH to LOW resets survey`() {
        every { riskLevelStorage.latestEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH.plus(1)),
                createEwRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH)
            )
        )
        every { riskLevelStorage.latestCombinedEwPtRiskLevelResults } returns
            flowOf(listOf(createCombinedRiskLevel(LOW_RISK)))
        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                surveys.resetSurvey(Surveys.Type.HIGH_RISK_ENCOUNTER)
            }
        }
    }

    @Test
    fun `risk level went from LOW to HIGH`() {
        every { riskLevelStorage.latestEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH.plus(1)),
                createEwRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
            )
        )
        every { riskLevelStorage.latestCombinedEwPtRiskLevelResults } returns
            flowOf(listOf(createCombinedRiskLevel(LOW_RISK)))

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                surveys wasNot Called
            }
        }
    }

    @Test
    fun `risk level went from LOW to HIGH but it is has already been processed`() {
        every { riskLevelStorage.latestEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH.plus(1)),
                createEwRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
            )
        )
        every { riskLevelStorage.latestCombinedEwPtRiskLevelResults } returns
            flowOf(listOf(createCombinedRiskLevel(LOW_RISK)))
        every { riskLevelSettings.lastChangeCheckedRiskLevelTimestamp } returns Instant.EPOCH.plus(1)

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                notificationManagerCompat wasNot Called
                surveys wasNot Called
            }
        }
    }

    @Test
    fun `combined risk level went from LOW to HIGH but it is has already been processed`() {
        every { riskLevelStorage.latestEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(LOW_RISK)
            )
        )

        every { riskLevelStorage.latestCombinedEwPtRiskLevelResults } returns
            flowOf(
                listOf(
                    createCombinedRiskLevel(INCREASED_RISK, calculatedAt = Instant.EPOCH.plus(1)),
                    createCombinedRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
                )
            )

        every { riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp } returns Instant.EPOCH.plus(1)

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()

            advanceUntilIdle()

            coVerifySequence {
                notificationManagerCompat wasNot Called
                surveys wasNot Called
            }
        }
    }

    @Test
    fun `riskLevelTurnedRedTime is only set once`() {
        testResultDonorSettings.riskLevelTurnedRedTime.update { Instant.EPOCH.plus(1) }

        every { riskLevelStorage.latestEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(
                    INCREASED_RISK,
                    calculatedAt = Instant.EPOCH.plus(2),
                    ewAggregatedRiskResult = mockk<EwAggregatedRiskResult>().apply {
                        every { isIncreasedRisk() } returns true
                    }
                ),
                createEwRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
            )
        )

        every { riskLevelStorage.latestCombinedEwPtRiskLevelResults } returns
            flowOf(listOf(createCombinedRiskLevel(LOW_RISK)))

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()
            advanceUntilIdle()
        }

        testResultDonorSettings.riskLevelTurnedRedTime.value shouldBe Instant.EPOCH.plus(1)

        testResultDonorSettings.riskLevelTurnedRedTime.update { null }

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()
            advanceUntilIdle()
        }

        testResultDonorSettings.riskLevelTurnedRedTime.value shouldBe Instant.EPOCH.plus(2)
    }

    @Test
    fun `mostRecentDateWithHighOrLowRiskLevel is updated every time`() {
        every { riskLevelStorage.latestEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(
                    INCREASED_RISK,
                    calculatedAt = Instant.EPOCH.plus(1),
                    ewAggregatedRiskResult = mockk<EwAggregatedRiskResult>().apply {
                        every { mostRecentDateWithHighRisk } returns Instant.EPOCH.plus(10)
                        every { isIncreasedRisk() } returns true
                    }
                ),
                createEwRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
            )
        )
        every { riskLevelStorage.latestCombinedEwPtRiskLevelResults } returns
            flowOf(listOf(createCombinedRiskLevel(LOW_RISK)))

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()
            advanceUntilIdle()
        }

        testResultDonorSettings.mostRecentDateWithHighOrLowRiskLevel.value shouldBe Instant.EPOCH.plus(10)

        every { riskLevelStorage.latestEwRiskLevelResults } returns flowOf(
            listOf(
                createEwRiskLevel(
                    INCREASED_RISK,
                    calculatedAt = Instant.EPOCH.plus(1),
                    ewAggregatedRiskResult = mockk<EwAggregatedRiskResult>().apply {
                        every { mostRecentDateWithLowRisk } returns Instant.EPOCH.plus(20)
                        every { isIncreasedRisk() } returns false
                    }
                ),
                createEwRiskLevel(LOW_RISK, calculatedAt = Instant.EPOCH)
            )
        )

        runBlockingTest {
            val instance = createInstance(scope = this)
            instance.launch()
            advanceUntilIdle()
        }

        testResultDonorSettings.mostRecentDateWithHighOrLowRiskLevel.value shouldBe Instant.EPOCH.plus(20)
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
