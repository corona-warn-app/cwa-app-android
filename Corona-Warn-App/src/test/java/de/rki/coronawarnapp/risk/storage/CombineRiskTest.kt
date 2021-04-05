package de.rki.coronawarnapp.risk.storage

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.presencetracing.risk.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test

class CombineRiskTest {

    @Test
    fun `combineRisk works`() {
        val ptRisk = PresenceTracingDayRisk(
            localDateUtc = LocalDate(2021, 3, 20),
            riskState = RiskState.INCREASED_RISK
        )
        val ptRisk2 = PresenceTracingDayRisk(
            localDateUtc = LocalDate(2021, 3, 21),
            riskState = RiskState.LOW_RISK
        )
        val ptRisk3 = PresenceTracingDayRisk(
            localDateUtc = LocalDate(2021, 3, 22),
            riskState = RiskState.CALCULATION_FAILED
        )
        val ewRisk = ExposureWindowDayRisk(
            dateMillisSinceEpoch = Instant.parse("2021-03-22T14:00:00.000Z").millis,
            riskLevel = RiskLevel.HIGH,
            0,
            0
        )
        val ewRisk2 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = Instant.parse("2021-03-19T14:00:00.000Z").millis,
            riskLevel = RiskLevel.LOW,
            0,
            0
        )
        val ewRisk3 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = Instant.parse("2021-03-20T14:00:00.000Z").millis,
            riskLevel = RiskLevel.UNSPECIFIED,
            0,
            0
        )
        val ewRisk4 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = Instant.parse("2021-03-15T14:00:00.000Z").millis,
            riskLevel = RiskLevel.UNSPECIFIED,
            0,
            0
        )

        val ptDayRiskList: List<PresenceTracingDayRisk> = listOf(ptRisk, ptRisk2, ptRisk3)
        val ewDayRiskList: List<ExposureWindowDayRisk> = listOf(ewRisk, ewRisk2, ewRisk3, ewRisk4)
        val result = combineRisk(ptDayRiskList, ewDayRiskList)
        result.size shouldBe 5
        result.find {
            it.localDate == LocalDate(2021, 3, 15)
        }!!.riskState shouldBe RiskState.CALCULATION_FAILED
        result.find {
            it.localDate == LocalDate(2021, 3, 19)
        }!!.riskState shouldBe RiskState.LOW_RISK
        result.find {
            it.localDate == LocalDate(2021, 3, 20)
        }!!.riskState shouldBe RiskState.INCREASED_RISK
        result.find {
            it.localDate == LocalDate(2021, 3, 21)
        }!!.riskState shouldBe RiskState.LOW_RISK
        result.find {
            it.localDate == LocalDate(2021, 3, 22)
        }!!.riskState shouldBe RiskState.INCREASED_RISK
    }

    @Test
    fun `combineEwPtRiskLevelResults works`() {
        val startInstant = Instant.ofEpochMilli(10000)

        val ptResult = PtRiskLevelResult(
            calculatedAt = startInstant.plus(1000L),
            riskState = RiskState.LOW_RISK
        )
        val ptResult2 = PtRiskLevelResult(
            calculatedAt = startInstant.plus(3000L),
            riskState = RiskState.CALCULATION_FAILED
        )
        val ptResult3 = PtRiskLevelResult(
            calculatedAt = startInstant.plus(6000L),
            riskState = RiskState.CALCULATION_FAILED
        )
        val ptResult4 = PtRiskLevelResult(
            calculatedAt = startInstant.plus(7000L),
            riskState = RiskState.CALCULATION_FAILED
        )

        val ptResults = listOf(ptResult, ptResult2, ptResult4, ptResult3)
        val ewResult = createEwRiskLevelResult(
            calculatedAt = startInstant.plus(2000L),
            riskState = RiskState.CALCULATION_FAILED
        )
        val ewResult2 = createEwRiskLevelResult(
            calculatedAt = startInstant.plus(4000L),
            riskState = RiskState.INCREASED_RISK
        )
        val ewResult3 = createEwRiskLevelResult(
            calculatedAt = startInstant.plus(5000L),
            riskState = RiskState.CALCULATION_FAILED
        )
        val ewResult4 = createEwRiskLevelResult(
            calculatedAt = startInstant.plus(8000L),
            riskState = RiskState.CALCULATION_FAILED
        )
        val ewResults = listOf(ewResult, ewResult4, ewResult2, ewResult3)
        val result = combineEwPtRiskLevelResults(ptResults, ewResults).sortedByDescending { it.calculatedAt }
        result.size shouldBe 8
        result[0].riskState shouldBe RiskState.CALCULATION_FAILED
        result[0].calculatedAt shouldBe startInstant.plus(8000L)
        result[1].riskState shouldBe RiskState.CALCULATION_FAILED
        result[1].calculatedAt shouldBe startInstant.plus(7000L)
        result[2].riskState shouldBe RiskState.CALCULATION_FAILED
        result[2].calculatedAt shouldBe startInstant.plus(6000L)
        result[3].riskState shouldBe RiskState.CALCULATION_FAILED
        result[3].calculatedAt shouldBe startInstant.plus(5000L)
        result[4].riskState shouldBe RiskState.INCREASED_RISK
        result[4].calculatedAt shouldBe startInstant.plus(4000L)
        result[5].riskState shouldBe RiskState.CALCULATION_FAILED
        result[5].calculatedAt shouldBe startInstant.plus(3000L)
        result[6].riskState shouldBe RiskState.LOW_RISK
        result[6].calculatedAt shouldBe startInstant.plus(2000L)
        result[7].riskState shouldBe RiskState.LOW_RISK
        result[7].calculatedAt shouldBe startInstant.plus(1000L)
    }

    @Test
    fun `max RiskState works`() {
        combine(RiskState.INCREASED_RISK, RiskState.INCREASED_RISK) shouldBe RiskState.INCREASED_RISK
        combine(RiskState.INCREASED_RISK, RiskState.LOW_RISK) shouldBe RiskState.INCREASED_RISK
        combine(RiskState.INCREASED_RISK, RiskState.CALCULATION_FAILED) shouldBe RiskState.INCREASED_RISK
        combine(RiskState.LOW_RISK, RiskState.INCREASED_RISK) shouldBe RiskState.INCREASED_RISK
        combine(RiskState.CALCULATION_FAILED, RiskState.INCREASED_RISK) shouldBe RiskState.INCREASED_RISK
        combine(RiskState.LOW_RISK, RiskState.LOW_RISK) shouldBe RiskState.LOW_RISK
        combine(RiskState.CALCULATION_FAILED, RiskState.LOW_RISK) shouldBe RiskState.LOW_RISK
        combine(RiskState.CALCULATION_FAILED, RiskState.CALCULATION_FAILED) shouldBe RiskState.CALCULATION_FAILED
    }
}

private fun createEwRiskLevelResult(
    calculatedAt: Instant,
    riskState: RiskState
): EwRiskLevelResult = object : EwRiskLevelResult {
    override val calculatedAt: Instant = calculatedAt
    override val riskState: RiskState = riskState
    override val failureReason: EwRiskLevelResult.FailureReason? = null
    override val ewAggregatedRiskResult: EwAggregatedRiskResult? = null
    override val exposureWindows: List<ExposureWindow>? = null
    override val matchedKeyCount: Int = 0
    override val daysWithEncounters: Int = 0
}
