package de.rki.coronawarnapp.tracing.ui.details

import android.content.Context
import android.content.res.Resources
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.installTime.InstallTimeProvider
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelTaskResult
import de.rki.coronawarnapp.risk.ProtoRiskLevel
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.CombinedEwPtRiskLevelResult
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.test

class TracingDetailsItemProviderTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context
    @MockK(relaxed = true) lateinit var resources: Resources
    @MockK(relaxed = true) lateinit var ewAggregatedRiskResult: EwAggregatedRiskResult

    @MockK lateinit var tracingStatus: GeneralTracingStatus
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var installTimeProvider: InstallTimeProvider
    @MockK lateinit var surveys: Surveys

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
    }

    private fun createInstance() = TracingDetailsItemProvider(
        tracingStatus = tracingStatus,
        riskLevelStorage = riskLevelStorage,
        installTimeProvider = installTimeProvider,
        surveys = surveys
    )

    private fun prepare(
        status: GeneralTracingStatus.Status,
        riskLevel: ProtoRiskLevel,
        matchedKeyCount: Int,
        daysSinceInstallation: Long,
        availableSurveys: List<Surveys.Type> = emptyList()
    ) {
        every { tracingStatus.generalStatus } returns flowOf(status)
        every { ewAggregatedRiskResult.totalRiskLevel } returns riskLevel
        every { installTimeProvider.daysSinceInstallation } returns daysSinceInstallation
        every { surveys.availableSurveys } returns flowOf(availableSurveys)

        if (riskLevel == ProtoRiskLevel.LOW) {
            every { ewAggregatedRiskResult.isLowRisk() } returns true
        } else if (riskLevel == ProtoRiskLevel.HIGH) {
            every { ewAggregatedRiskResult.isIncreasedRisk() } returns true
        }

        val exposureWindow: ExposureWindow = mockk()

        val ewRiskLevelTaskResult = EwRiskLevelTaskResult(
            calculatedAt = Instant.EPOCH,
            ewAggregatedRiskResult = ewAggregatedRiskResult,
            exposureWindows = listOf(exposureWindow)
        )

        val ptRiskLevelResult = PtRiskLevelResult(
            calculatedAt = Instant.EPOCH,
            riskState = RiskState.CALCULATION_FAILED
        )
        val combined = CombinedEwPtRiskLevelResult(
            ewRiskLevelResult = ewRiskLevelTaskResult,
            ptRiskLevelResult = ptRiskLevelResult
        )
        every { ewRiskLevelTaskResult.matchedKeyCount } returns matchedKeyCount
        every { riskLevelStorage.latestAndLastSuccessfulEwRiskLevelResult } returns flowOf(listOf(ewRiskLevelTaskResult))
        // TODO tests
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(listOf(combined))
    }

    @Test
    fun `additional info low risk box`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.LOW,
            daysSinceInstallation = 4,
            matchedKeyCount = 1
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe true
    }

    @Test
    fun `no additional info low risk box due to matched key count`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.LOW,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
    }

    @Test
    fun `no additional info low risk box due to high risk`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.HIGH,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
    }

    @Test
    fun `increased risk box and no normal risk box`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.HIGH,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorIncreasedRiskBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is BehaviorNormalRiskBox.Item } shouldBe false
    }

    @Test
    fun `normal risk box and no increased risk box`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.LOW,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorNormalRiskBox.Item } shouldBe true
    }

    @Test
    fun `period logged box with low risk`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.LOW,
            daysSinceInstallation = 4,
            matchedKeyCount = 0,
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorNormalRiskBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is PeriodLoggedBox.Item } shouldBe true
    }

    @Test
    fun `period logged box with high risk`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.HIGH,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorIncreasedRiskBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is BehaviorNormalRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is PeriodLoggedBox.Item } shouldBe true
    }

    @Test
    fun `no period logged box due to failed calculation`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.UNRECOGNIZED,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorNormalRiskBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is PeriodLoggedBox.Item } shouldBe false
    }

    @Test
    fun `no period logged box due to inactive tracing`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_INACTIVE,
            riskLevel = ProtoRiskLevel.LOW,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorNormalRiskBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is PeriodLoggedBox.Item } shouldBe false
    }

    @Test
    fun `failed calculation box due to inactive tracing`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_INACTIVE,
            riskLevel = ProtoRiskLevel.LOW,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorNormalRiskBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is PeriodLoggedBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is DetailsFailedCalculationBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is DetailsLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is DetailsIncreasedRiskBox.Item } shouldBe false
    }

    @Test
    fun `failed calculation box due to failed calculation`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.UNRECOGNIZED,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorNormalRiskBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is PeriodLoggedBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is DetailsFailedCalculationBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is DetailsLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is DetailsIncreasedRiskBox.Item } shouldBe false
    }

    @Test
    fun `low risk box no high risk box`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.LOW,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorIncreasedRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorNormalRiskBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is PeriodLoggedBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is DetailsFailedCalculationBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is DetailsLowRiskBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is DetailsIncreasedRiskBox.Item } shouldBe false
    }

    @Test
    fun `high risk box no low risk box`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.HIGH,
            daysSinceInstallation = 4,
            matchedKeyCount = 0
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.size.shouldBeGreaterThan(0)
        testCollector.latestValue!!.any { it is AdditionalInfoLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is BehaviorIncreasedRiskBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is BehaviorNormalRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is PeriodLoggedBox.Item } shouldBe true
        testCollector.latestValue!!.any { it is DetailsFailedCalculationBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is DetailsLowRiskBox.Item } shouldBe false
        testCollector.latestValue!!.any { it is DetailsIncreasedRiskBox.Item } shouldBe true
    }

    @Test
    fun `low risk no high risk survey box`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.LOW,
            matchedKeyCount = 0,
            daysSinceInstallation = 4,
            availableSurveys = listOf(Surveys.Type.HIGH_RISK_ENCOUNTER)
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.run {
            any { it is DetailsLowRiskBox.Item } shouldBe true
            any { it is DetailsIncreasedRiskBox.Item } shouldBe false
            any { it is UserSurveyBox.Item } shouldBe false
        }
    }

    @Test
    fun `high risk but feature disabled so no high risk survey box`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.HIGH,
            matchedKeyCount = 0,
            daysSinceInstallation = 4,
            availableSurveys = emptyList()
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.run {
            any { it is DetailsLowRiskBox.Item } shouldBe false
            any { it is DetailsIncreasedRiskBox.Item } shouldBe true
            any { it is UserSurveyBox.Item } shouldBe false
        }
    }

    @Test
    fun `high risk and feature enabled so high risk survey box`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.HIGH,
            matchedKeyCount = 0,
            daysSinceInstallation = 4,
            availableSurveys = listOf(Surveys.Type.HIGH_RISK_ENCOUNTER)
        )

        val instance = createInstance()
        val testCollector = instance.state.test(startOnScope = this)

        testCollector.latestValue!!.run {
            any { it is DetailsLowRiskBox.Item } shouldBe false
            any { it is DetailsIncreasedRiskBox.Item } shouldBe true
            any { it is UserSurveyBox.Item } shouldBe true
        }
    }
}
