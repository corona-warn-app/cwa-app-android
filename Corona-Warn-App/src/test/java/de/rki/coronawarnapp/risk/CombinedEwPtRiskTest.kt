package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.minusDaysAtStartOfDayUtc
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.util.toLocalDateUtc
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

class CombinedEwPtRiskTest : BaseTest() {

    @MockK lateinit var ewAggregatedRiskResult: EwAggregatedRiskResult

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `counts high risk days correctly`() {
        val ewDayRisk = ExposureWindowDayRisk(
            dateMillisSinceEpoch = 1000,
            riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            minimumDistinctEncountersWithLowRisk = 0,
            minimumDistinctEncountersWithHighRisk = 1
        )
        val ewDayRisk2 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = 1000 + MILLIS_DAY,
            riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
            minimumDistinctEncountersWithLowRisk = 1,
            minimumDistinctEncountersWithHighRisk = 0
        )
        val ewDayRisk3 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = 1000 + 2 * MILLIS_DAY,
            riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            minimumDistinctEncountersWithLowRisk = 1,
            minimumDistinctEncountersWithHighRisk = 2
        )
        every { ewAggregatedRiskResult.exposureWindowDayRisks } returns listOf(ewDayRisk, ewDayRisk2, ewDayRisk3)

        val ptDayRisk = PresenceTracingDayRisk(
            riskState = RiskState.INCREASED_RISK,
            localDateUtc = Instant.ofEpochMilli(1000).toLocalDateUtc()
        )
        val ptDayRisk2 = PresenceTracingDayRisk(
            riskState = RiskState.LOW_RISK,
            localDateUtc = Instant.ofEpochMilli(1000 + MILLIS_DAY).toLocalDateUtc()
        )
        val ptDayRisk3 = PresenceTracingDayRisk(
            riskState = RiskState.INCREASED_RISK,
            localDateUtc = Instant.ofEpochMilli(1000 + 2 * MILLIS_DAY).toLocalDateUtc()
        )

        every { ewAggregatedRiskResult.isIncreasedRisk() } returns true

        CombinedEwPtRiskLevelResult(
            ptRiskLevelResult = createPtRiskLevelResult(
                calculatedAt = Instant.ofEpochMilli(1000 + 2 * MILLIS_DAY),
                riskState = RiskState.LOW_RISK,
                presenceTracingDayRisk = listOf(ptDayRisk, ptDayRisk2, ptDayRisk3)
            ),
            ewRiskLevelResult = createEwRiskLevel(
                calculatedAt = Instant.ofEpochMilli(1000 + 2 * MILLIS_DAY),
                ewAggregatedRiskResult
            )
        ).daysWithEncounters shouldBe 2
    }

    @Test
    fun `counts low risk days correctly`() {
        val ewDayRisk = ExposureWindowDayRisk(
            dateMillisSinceEpoch = 1000,
            riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
            minimumDistinctEncountersWithLowRisk = 0,
            minimumDistinctEncountersWithHighRisk = 1
        )
        val ewDayRisk2 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = 1000 + MILLIS_DAY,
            riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
            minimumDistinctEncountersWithLowRisk = 1,
            minimumDistinctEncountersWithHighRisk = 0
        )

        val ptDayRisk = PresenceTracingDayRisk(
            riskState = RiskState.LOW_RISK,
            localDateUtc = Instant.ofEpochMilli(1000).toLocalDateUtc()
        )

        val ptDayRisk3 = PresenceTracingDayRisk(
            riskState = RiskState.LOW_RISK,
            localDateUtc = Instant.ofEpochMilli(1000 + 2 * MILLIS_DAY).toLocalDateUtc()
        )

        every { ewAggregatedRiskResult.isLowRisk() } returns true
        every { ewAggregatedRiskResult.isIncreasedRisk() } returns false

        CombinedEwPtRiskLevelResult(
            ptRiskLevelResult = createPtRiskLevelResult(
                calculatedAt = Instant.ofEpochMilli(1000 + 2 * MILLIS_DAY),
                riskState = RiskState.LOW_RISK,
                presenceTracingDayRisk = listOf(ptDayRisk, ptDayRisk3)
            ),
            ewRiskLevelResult = createEwRiskLevel(
                calculatedAt = Instant.ofEpochMilli(1000 + 2 * MILLIS_DAY),
                ewAggregatedRiskResult
            ),
            exposureWindowDayRisks = listOf(ewDayRisk, ewDayRisk2)
        ).daysWithEncounters shouldBe 3
    }

    @Test
    fun `counts days correctly`() {
        val dayRisk = ExposureWindowDayRisk(
            dateMillisSinceEpoch = 1000,
            riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            minimumDistinctEncountersWithLowRisk = 0,
            minimumDistinctEncountersWithHighRisk = 1
        )
        val dayRisk2 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = 1000 + MILLIS_DAY,
            riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
            minimumDistinctEncountersWithLowRisk = 1,
            minimumDistinctEncountersWithHighRisk = 0
        )
        val dayRisk3 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = 1000 + 2 * MILLIS_DAY,
            riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            minimumDistinctEncountersWithLowRisk = 1,
            minimumDistinctEncountersWithHighRisk = 2
        )

        val result = CombinedEwPtRiskLevelResult(
            ptRiskLevelResult = createPtRiskLevelResult(
                calculatedAt = Instant.ofEpochMilli(1000 + 2 * MILLIS_DAY),
                riskState = RiskState.LOW_RISK,
                presenceTracingDayRisk = listOf()
            ),
            ewRiskLevelResult = createEwRiskLevel(
                calculatedAt = Instant.ofEpochMilli(1000 + 2 * MILLIS_DAY),
                ewAggregatedRiskResult
            ),
            exposureWindowDayRisks = listOf(dayRisk, dayRisk2, dayRisk3)
        )

        result.ewDaysWithHighRisk.size shouldBe 2
        result.ewDaysWithLowRisk.size shouldBe 1
    }

    private fun createPtRiskLevelResult(
        calculatedAt: Instant,
        riskState: RiskState,
        presenceTracingDayRisk: List<PresenceTracingDayRisk>
    ): PtRiskLevelResult = PtRiskLevelResult(
        calculatedAt = calculatedAt,
        riskState = riskState,
        presenceTracingDayRisk = presenceTracingDayRisk,
        calculatedFrom = calculatedAt.minusDaysAtStartOfDayUtc(10).toInstant()
    )

    private fun createEwRiskLevel(
        calculatedAt: Instant,
        ewAggregatedRiskResult: EwAggregatedRiskResult?
    ): EwRiskLevelResult = object : EwRiskLevelResult {
        override val calculatedAt = calculatedAt
        override val ewAggregatedRiskResult: EwAggregatedRiskResult? = ewAggregatedRiskResult
        override val failureReason: EwRiskLevelResult.FailureReason? = null
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
    }
}

private const val MILLIS_DAY = (1000 * 60 * 60 * 24).toLong()
