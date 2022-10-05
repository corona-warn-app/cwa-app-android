package de.rki.coronawarnapp.tracing.ui.details

import android.content.Context
import android.content.res.Resources
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.installTime.InstallTimeProvider
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelTaskResult
import de.rki.coronawarnapp.risk.LastCombinedRiskResults
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos.AdditionalInfoLowRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorIncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorNormalRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.periodlogged.PeriodLoggedBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsFailedCalculationBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsIncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsLowRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.survey.UserSurveyBox
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.test
import java.time.Instant

class TracingDetailsItemProviderTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context
    @MockK(relaxed = true) lateinit var resources: Resources
    @MockK(relaxed = true) lateinit var ewAggregatedRiskResult: EwAggregatedRiskResult

    @MockK lateinit var tracingStatus: GeneralTracingStatus
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var installTimeProvider: InstallTimeProvider
    @MockK lateinit var surveys: Surveys
    @MockK lateinit var appConfigProvider: AppConfigProvider

    @MockK(relaxed = true) lateinit var combinedResult: CombinedEwPtRiskLevelResult

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        every { appConfigProvider.currentConfig } returns flowOf(
            mockk<ConfigData>().apply {
                every { maxEncounterAgeInDays } returns 10
            }
        )
    }

    private fun createInstance() = TracingDetailsItemProvider(
        tracingStatus = tracingStatus,
        riskLevelStorage = riskLevelStorage,
        installTimeProvider = installTimeProvider,
        surveys = surveys,
        appConfigProvider = appConfigProvider,
    )

    private fun prepare(
        status: GeneralTracingStatus.Status,
        riskState: RiskState,
        matchedKeyCount: Int,
        daysSinceInstallation: Int,
        availableSurveys: List<Surveys.Type> = emptyList()
    ) {
        every { tracingStatus.generalStatus } returns flowOf(status)
        every { installTimeProvider.daysSinceInstallation } returns daysSinceInstallation
        every { surveys.availableSurveys } returns flowOf(availableSurveys)

        if (riskState == RiskState.LOW_RISK) {
            every { ewAggregatedRiskResult.isLowRisk() } returns true
        } else if (riskState == RiskState.INCREASED_RISK) {
            every { ewAggregatedRiskResult.isIncreasedRisk() } returns true
        }

        every { combinedResult.riskState } returns riskState

        val exposureWindow: ExposureWindow = mockk()

        val ewRiskLevelTaskResult = EwRiskLevelTaskResult(
            calculatedAt = Instant.EPOCH,
            ewAggregatedRiskResult = ewAggregatedRiskResult,
            exposureWindows = listOf(exposureWindow)
        )

        every { combinedResult.matchedRiskCount } returns matchedKeyCount
        every { combinedResult.daysWithEncounters } returns matchedKeyCount

        val lastCombined = LastCombinedRiskResults(
            lastCalculated = combinedResult,
            lastSuccessfullyCalculatedRiskState = combinedResult.riskState
        )
        every { ewRiskLevelTaskResult.matchedKeyCount } returns matchedKeyCount
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(lastCombined)
    }

    @Test
    fun `additional info low risk box`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.LOW_RISK,
            daysSinceInstallation = 4,
            matchedKeyCount = 1
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe true
    }

    @Test
    fun `no additional info low risk box due to matched key count`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.LOW_RISK,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
    }

    @Test
    fun `no additional info low risk box due to high risk`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.INCREASED_RISK,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
    }

    @Test
    fun `increased risk box and no normal risk box`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.INCREASED_RISK,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorIncreasedRiskBox.Item } shouldBe true
        instance.state.first().any { it is BehaviorNormalRiskBox.Item } shouldBe false
    }

    @Test
    fun `normal risk box and no increased risk box`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.LOW_RISK,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorNormalRiskBox.Item } shouldBe true
    }

    @Test
    fun `period logged box with low risk`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.LOW_RISK,
            daysSinceInstallation = 4,
            matchedKeyCount = 0,
        )

        val instance = createInstance()
        instance.state.test(startOnScope = this)

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorNormalRiskBox.Item } shouldBe true
        instance.state.first().any { it is PeriodLoggedBox.Item } shouldBe true
    }

    @Test
    fun `period logged box with high risk`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.INCREASED_RISK,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorIncreasedRiskBox.Item } shouldBe true
        instance.state.first().any { it is BehaviorNormalRiskBox.Item } shouldBe false
        instance.state.first().any { it is PeriodLoggedBox.Item } shouldBe true
    }

    @Test
    fun `no period logged box due to failed calculation`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.CALCULATION_FAILED,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorNormalRiskBox.Item } shouldBe true
        instance.state.first().any { it is PeriodLoggedBox.Item } shouldBe false
    }

    @Test
    fun `no period logged box due to inactive tracing`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_INACTIVE,
            riskState = RiskState.LOW_RISK,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorNormalRiskBox.Item } shouldBe true
        instance.state.first().any { it is PeriodLoggedBox.Item } shouldBe false
    }

    @Test
    fun `failed calculation box due to inactive tracing`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_INACTIVE,
            riskState = RiskState.LOW_RISK,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorNormalRiskBox.Item } shouldBe true
        instance.state.first().any { it is PeriodLoggedBox.Item } shouldBe false
        instance.state.first().any { it is DetailsFailedCalculationBox.Item } shouldBe true
        instance.state.first().any { it is DetailsLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is DetailsIncreasedRiskBox.Item } shouldBe false
    }

    @Test
    fun `failed calculation box due to failed calculation`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.CALCULATION_FAILED,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorNormalRiskBox.Item } shouldBe true
        instance.state.first().any { it is PeriodLoggedBox.Item } shouldBe false
        instance.state.first().any { it is DetailsFailedCalculationBox.Item } shouldBe true
        instance.state.first().any { it is DetailsLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is DetailsIncreasedRiskBox.Item } shouldBe false
    }

    @Test
    fun `low risk box no high risk box`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.LOW_RISK,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorNormalRiskBox.Item } shouldBe true
        instance.state.first().any { it is PeriodLoggedBox.Item } shouldBe true
        instance.state.first().any { it is DetailsFailedCalculationBox.Item } shouldBe false
        instance.state.first().any { it is DetailsLowRiskBox.Item } shouldBe true
        instance.state.first().any { it is DetailsIncreasedRiskBox.Item } shouldBe false
    }

    @Test
    fun `high risk box no low risk box`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.INCREASED_RISK,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()

        instance.state.first().size.shouldBeGreaterThan(0)
        instance.state.first().any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is BehaviorIncreasedRiskBox.Item } shouldBe true
        instance.state.first().any { it is BehaviorNormalRiskBox.Item } shouldBe false
        instance.state.first().any { it is PeriodLoggedBox.Item } shouldBe true
        instance.state.first().any { it is DetailsFailedCalculationBox.Item } shouldBe false
        instance.state.first().any { it is DetailsLowRiskBox.Item } shouldBe false
        instance.state.first().any { it is DetailsIncreasedRiskBox.Item } shouldBe true
    }

    @Test
    fun `low risk no high risk survey box`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.LOW_RISK,
            matchedKeyCount = 0,
            daysSinceInstallation = 4,
            availableSurveys = listOf(Surveys.Type.HIGH_RISK_ENCOUNTER)
        )

        val instance = createInstance()

        instance.state.first().run {
            any { it is DetailsLowRiskBox.Item } shouldBe true
            any { it is DetailsIncreasedRiskBox.Item } shouldBe false
            any { it is UserSurveyBox.Item } shouldBe false
        }
    }

    @Test
    fun `high risk but feature disabled so no high risk survey box`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.INCREASED_RISK,
            matchedKeyCount = 0,
            daysSinceInstallation = 4,
            availableSurveys = emptyList()
        )

        val instance = createInstance()

        instance.state.first().run {
            any { it is DetailsLowRiskBox.Item } shouldBe false
            any { it is DetailsIncreasedRiskBox.Item } shouldBe true
            any { it is UserSurveyBox.Item } shouldBe false
        }
    }

    @Test
    fun `high risk and feature enabled so high risk survey box`() = runTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskState = RiskState.INCREASED_RISK,
            matchedKeyCount = 0,
            daysSinceInstallation = 4,
            availableSurveys = listOf(Surveys.Type.HIGH_RISK_ENCOUNTER)
        )

        val instance = createInstance()

        instance.state.first().run {
            any { it is DetailsLowRiskBox.Item } shouldBe false
            any { it is DetailsIncreasedRiskBox.Item } shouldBe true
            any { it is UserSurveyBox.Item } shouldBe true
        }
    }
}
