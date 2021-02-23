package de.rki.coronawarnapp.datadonation.analytics.modules

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.exposureriskmetadata.ExposureRiskMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import testhelpers.preferences.mockFlowPreference

class ExposureRiskMetadataDonorTest : BaseTest() {
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var highAggregatedRiskResult: AggregatedRiskResult
    @MockK lateinit var lowAggregatedRiskResult: AggregatedRiskResult

    private val baseDate: Instant = Instant.ofEpochMilli(101010)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { highAggregatedRiskResult.isIncreasedRisk() } returns true
        every { highAggregatedRiskResult.mostRecentDateWithHighRisk } returns baseDate
        every { lowAggregatedRiskResult.isIncreasedRisk() } returns false
        every { lowAggregatedRiskResult.mostRecentDateWithHighRisk } returns baseDate
    }

    private fun createRiskLevelResult(
        aggregatedRiskResult: AggregatedRiskResult?,
        failureReason: RiskLevelResult.FailureReason?,
        calculatedAt: Instant
    ): RiskLevelResult = object : RiskLevelResult {
        override val calculatedAt: Instant = calculatedAt
        override val aggregatedRiskResult: AggregatedRiskResult? = aggregatedRiskResult
        override val failureReason: RiskLevelResult.FailureReason? = failureReason
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
        override val daysWithEncounters: Int = 0
    }

    private fun createInstance() = ExposureRiskMetadataDonor(
        riskLevelStorage = riskLevelStorage,
        analyticsSettings = analyticsSettings
    )

    @Test
    fun `risk metadata is properly collected`() {
        val expectedMetadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setMostRecentDateAtRiskLevel(baseDate.seconds)
            .setRiskLevelChangedComparedToPreviousSubmission(true)
            .setDateChangedComparedToPreviousSubmission(true)
            .build()

        every { analyticsSettings.previousExposureRiskMetadata } returns mockFlowPreference(null)
        every { riskLevelStorage.latestAndLastSuccessful } returns flowOf(
            listOf(
                createRiskLevelResult(
                    aggregatedRiskResult = highAggregatedRiskResult,
                    failureReason = null,
                    calculatedAt = baseDate
                ),
                createRiskLevelResult(
                    aggregatedRiskResult = lowAggregatedRiskResult,
                    failureReason = RiskLevelResult.FailureReason.UNKNOWN,
                    calculatedAt = baseDate
                )
            )
        )

        val parentBuilder = PpaData.PPADataAndroid.newBuilder()

        runBlockingTest2 {
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
    fun `risk metadata change is properly collected`() {
        val initialMetadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setMostRecentDateAtRiskLevel(baseDate.seconds)
            .setRiskLevelChangedComparedToPreviousSubmission(true)
            .setDateChangedComparedToPreviousSubmission(true)
            .build()

        val expectedMetadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setMostRecentDateAtRiskLevel(baseDate.seconds)
            .setRiskLevelChangedComparedToPreviousSubmission(false)
            .setDateChangedComparedToPreviousSubmission(false)
            .build()

        every { analyticsSettings.previousExposureRiskMetadata } returns mockFlowPreference(initialMetadata)

        every { riskLevelStorage.latestAndLastSuccessful } returns flowOf(
            listOf(
                createRiskLevelResult(
                    aggregatedRiskResult = highAggregatedRiskResult,
                    failureReason = null,
                    calculatedAt = baseDate
                ),
                createRiskLevelResult(
                    aggregatedRiskResult = lowAggregatedRiskResult,
                    failureReason = RiskLevelResult.FailureReason.UNKNOWN,
                    calculatedAt = baseDate
                )
            )
        )

        val parentBuilder = PpaData.PPADataAndroid.newBuilder()

        runBlockingTest2 {
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
}
