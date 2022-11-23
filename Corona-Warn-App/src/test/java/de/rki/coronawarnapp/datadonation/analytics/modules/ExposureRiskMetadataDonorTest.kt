package de.rki.coronawarnapp.datadonation.analytics.modules

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.exposureriskmetadata.ExposureRiskMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.minusDaysAtStartOfDayUtc
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.LastCombinedRiskResults
import de.rki.coronawarnapp.risk.LastSuccessfulRiskResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.toLocalDateUtc
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.preferences.FakeDataStore
import java.time.Instant
import java.time.ZoneId

class ExposureRiskMetadataDonorTest : BaseTest() {
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var highEwAggregatedRiskResult: EwAggregatedRiskResult
    @MockK lateinit var highPtDayRisk: PresenceTracingDayRisk
    @MockK lateinit var lowEwAggregatedRiskResult: EwAggregatedRiskResult
    @MockK lateinit var lowPtDayRisk: PresenceTracingDayRisk

    lateinit var analyticsSettings: AnalyticsSettings

    private val baseDate: Instant = Instant.ofEpochMilli(101010)

    private val ewResult = LastSuccessfulRiskResult(
        RiskState.LOW_RISK,
        baseDate
    )

    private val ptResult = LastSuccessfulRiskResult(
        RiskState.LOW_RISK,
        null
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { highEwAggregatedRiskResult.isIncreasedRisk() } returns true
        every { highEwAggregatedRiskResult.mostRecentDateWithHighRisk } returns baseDate
        every { lowEwAggregatedRiskResult.isIncreasedRisk() } returns false
        every { lowEwAggregatedRiskResult.mostRecentDateWithHighRisk } returns baseDate
        every { highPtDayRisk.riskState } returns RiskState.INCREASED_RISK
        every { highPtDayRisk.localDateUtc } returns baseDate.toLocalDateUtc()
        every { lowPtDayRisk.riskState } returns RiskState.LOW_RISK
        every { lowPtDayRisk.localDateUtc } returns baseDate.toLocalDateUtc()
        every { riskLevelStorage.lastSuccessfulEwRiskResult } returns flowOf(ewResult)
        every { riskLevelStorage.lastSuccessfulPtRiskResult } returns flowOf(ptResult)

        analyticsSettings = AnalyticsSettings(FakeDataStore())
    }

    private fun createEwRiskLevelResult(
        ewAggregatedRiskResult: EwAggregatedRiskResult?,
        failureReason: EwRiskLevelResult.FailureReason?,
        calculatedAt: Instant
    ): EwRiskLevelResult = object : EwRiskLevelResult {
        override val calculatedAt: Instant = calculatedAt
        override val ewAggregatedRiskResult: EwAggregatedRiskResult? = ewAggregatedRiskResult
        override val failureReason: EwRiskLevelResult.FailureReason? = failureReason
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
    }

    private fun createPtRiskLevelResult(
        riskState: RiskState,
        presenceTracingDayRisk: PresenceTracingDayRisk,
        calculatedAt: Instant = Instant.EPOCH,
    ): PtRiskLevelResult = PtRiskLevelResult(
        calculatedAt = calculatedAt,
        riskState = riskState,
        presenceTracingDayRisk = listOf(presenceTracingDayRisk),
        calculatedFrom = calculatedAt.minusDaysAtStartOfDayUtc(10).toInstant()
    )

    private fun createInstance() = ExposureRiskMetadataDonor(
        riskLevelStorage = riskLevelStorage,
        analyticsSettings = analyticsSettings
    )

    @Test
    fun `risk metadata is properly collected`() {
        val expectedMetadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setPtRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setMostRecentDateAtRiskLevel(baseDate.epochSecond)
            .setPtMostRecentDateAtRiskLevel(
                baseDate.toLocalDateUtc().atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
            )
            .setRiskLevelChangedComparedToPreviousSubmission(false)
            .setPtRiskLevelChangedComparedToPreviousSubmission(false)
            .setDateChangedComparedToPreviousSubmission(false)
            .setPtDateChangedComparedToPreviousSubmission(false)
            .build()

        every { riskLevelStorage.lastSuccessfulPtRiskResult } returns flowOf(
            LastSuccessfulRiskResult(
                RiskState.INCREASED_RISK,
                baseDate.toLocalDateUtc().atStartOfDay(ZoneId.systemDefault()).toInstant()
            )
        )

        every { riskLevelStorage.lastSuccessfulEwRiskResult } returns flowOf(
            LastSuccessfulRiskResult(
                RiskState.INCREASED_RISK,
                baseDate
            )
        )

        val parentBuilder = PpaData.PPADataAndroid.newBuilder()

        runTest2 {
            val contribution = createInstance().beginDonation(
                object : DonorModule.Request {
                    override val currentConfig: ConfigData = mockk()
                }
            )
            contribution.injectData(parentBuilder)
            contribution.finishDonation(true)
        }

        val parentProto = parentBuilder.build()

        parentProto.exposureRiskMetadataSetList[0] shouldBe expectedMetadata
    }

    @Test
    fun `risk metadata change is properly collected`() = runTest2 {
        val initialMetadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setMostRecentDateAtRiskLevel(baseDate.epochSecond)
            .setRiskLevelChangedComparedToPreviousSubmission(true)
            .setDateChangedComparedToPreviousSubmission(true)
            .setPtRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setPtMostRecentDateAtRiskLevel(
                baseDate.toLocalDateUtc().atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
            )
            .setPtRiskLevelChangedComparedToPreviousSubmission(true)
            .setPtDateChangedComparedToPreviousSubmission(true)
            .build()

        val expectedMetadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setMostRecentDateAtRiskLevel(baseDate.epochSecond)
            .setRiskLevelChangedComparedToPreviousSubmission(false)
            .setDateChangedComparedToPreviousSubmission(false)
            .setPtRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setPtMostRecentDateAtRiskLevel(
                baseDate.toLocalDateUtc().atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
            )
            .setPtRiskLevelChangedComparedToPreviousSubmission(false)
            .setPtDateChangedComparedToPreviousSubmission(false)
            .build()

        analyticsSettings.updatePreviousExposureRiskMetadata(initialMetadata)

        every { riskLevelStorage.lastSuccessfulPtRiskResult } returns flowOf(
            LastSuccessfulRiskResult(
                RiskState.INCREASED_RISK,
                baseDate.toLocalDateUtc().atStartOfDay(ZoneId.systemDefault()).toInstant()
            )
        )

        every { riskLevelStorage.lastSuccessfulEwRiskResult } returns flowOf(
            LastSuccessfulRiskResult(
                RiskState.INCREASED_RISK,
                baseDate
            )
        )

        val parentBuilder = PpaData.PPADataAndroid.newBuilder()

        val contribution = createInstance().beginDonation(
            object : DonorModule.Request {
                override val currentConfig: ConfigData = mockk()
            }
        )
        contribution.injectData(parentBuilder)
        contribution.finishDonation(true)

        val parentProto = parentBuilder.build()

        parentProto.exposureRiskMetadataSetList[0] shouldBe expectedMetadata
    }

    @Test
    fun `previous risk metadata is reset on success`() = runTest2 {
        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            LastCombinedRiskResults(
                lastSuccessfullyCalculatedRiskState = RiskState.INCREASED_RISK,
                lastCalculated = CombinedEwPtRiskLevelResult(
                    ptRiskLevelResult = createPtRiskLevelResult(
                        riskState = RiskState.INCREASED_RISK,
                        presenceTracingDayRisk = highPtDayRisk,
                        calculatedAt = baseDate
                    ),
                    ewRiskLevelResult = createEwRiskLevelResult(
                        ewAggregatedRiskResult = highEwAggregatedRiskResult,
                        failureReason = null,
                        calculatedAt = baseDate
                    )
                )
            )
        )

        val parentBuilder = PpaData.PPADataAndroid.newBuilder()

        val contribution = createInstance().beginDonation(
            object : DonorModule.Request {
                override val currentConfig: ConfigData = mockk()
            }
        )
        contribution.injectData(parentBuilder)
        contribution.finishDonation(true)

        val parentProto = parentBuilder.build()

        analyticsSettings.previousExposureRiskMetadata.first() shouldBe parentProto.exposureRiskMetadataSetList[0]
    }

    @Test
    fun `previous risk metadata is not reset on failure`() = runTest2 {
        val initialMetadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setMostRecentDateAtRiskLevel(baseDate.epochSecond)
            .setRiskLevelChangedComparedToPreviousSubmission(true)
            .setDateChangedComparedToPreviousSubmission(true)
            .setPtRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setPtMostRecentDateAtRiskLevel(
                baseDate.toLocalDateUtc().atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
            )
            .setPtRiskLevelChangedComparedToPreviousSubmission(true)
            .setPtDateChangedComparedToPreviousSubmission(true)
            .build()

        analyticsSettings.updatePreviousExposureRiskMetadata(initialMetadata)

        every { riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult } returns flowOf(
            LastCombinedRiskResults(
                lastSuccessfullyCalculatedRiskState = RiskState.INCREASED_RISK,
                lastCalculated = CombinedEwPtRiskLevelResult(
                    ptRiskLevelResult = createPtRiskLevelResult(
                        riskState = RiskState.INCREASED_RISK,
                        presenceTracingDayRisk = highPtDayRisk,
                        calculatedAt = baseDate
                    ),
                    ewRiskLevelResult = createEwRiskLevelResult(
                        ewAggregatedRiskResult = highEwAggregatedRiskResult,
                        failureReason = null,
                        calculatedAt = baseDate
                    )
                )
            )
        )

        val parentBuilder = PpaData.PPADataAndroid.newBuilder()
        val contribution = createInstance().beginDonation(
            object : DonorModule.Request {
                override val currentConfig: ConfigData = mockk()
            }
        )
        contribution.injectData(parentBuilder)
        contribution.finishDonation(false)

        analyticsSettings.previousExposureRiskMetadata.first() shouldBe initialMetadata
    }
}
