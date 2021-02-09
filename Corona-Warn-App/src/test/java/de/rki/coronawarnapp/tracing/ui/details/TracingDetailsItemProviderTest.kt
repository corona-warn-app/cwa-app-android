package de.rki.coronawarnapp.tracing.ui.details

import android.content.Context
import android.content.res.Resources
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.risk.ProtoRiskLevel
import de.rki.coronawarnapp.risk.RiskLevelTaskResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingRepository
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
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.test

class TracingDetailsItemProviderTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context
    @MockK(relaxed = true) lateinit var resources: Resources
    @MockK(relaxed = true) lateinit var aggregatedRiskResult: AggregatedRiskResult

    @MockK lateinit var tracingStatus: GeneralTracingStatus
    @MockK lateinit var tracingRepository: TracingRepository
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var surveys: Surveys

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = TracingDetailsItemProvider(
        tracingStatus = tracingStatus,
        tracingRepository = tracingRepository,
        riskLevelStorage = riskLevelStorage,
        surveys = surveys
    )

    private fun prepare(
        status: GeneralTracingStatus.Status,
        riskLevel: ProtoRiskLevel,
        matchedKeyCount: Int,
        availableSurveys: List<Surveys.Type> = emptyList()
    ) {
        every { tracingStatus.generalStatus } returns flowOf(status)
        every { tracingRepository.activeTracingDaysInRetentionPeriod } returns flowOf(0)
        every { aggregatedRiskResult.totalRiskLevel } returns riskLevel
        every { surveys.availableSurveys } returns flowOf(availableSurveys)

        if (riskLevel == ProtoRiskLevel.LOW) {
            every { aggregatedRiskResult.isLowRisk() } returns true
        } else if (riskLevel == ProtoRiskLevel.HIGH) {
            every { aggregatedRiskResult.isIncreasedRisk() } returns true
        }

        val exposureWindow: ExposureWindow = mockk()

        val riskLevelResult = RiskLevelTaskResult(
            calculatedAt = Instant.EPOCH,
            aggregatedRiskResult = aggregatedRiskResult,
            exposureWindows = listOf(exposureWindow)
        )
        every { riskLevelResult.matchedKeyCount } returns matchedKeyCount
        every { riskLevelStorage.latestAndLastSuccessful } returns flowOf(listOf(riskLevelResult))
    }

    @Test
    fun `additional info low risk box`() = runBlockingTest {

        prepare(
            status = GeneralTracingStatus.Status.TRACING_ACTIVE,
            riskLevel = ProtoRiskLevel.LOW,
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
            matchedKeyCount = 0
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
