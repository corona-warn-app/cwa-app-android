package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.presencetracing.risk.PresenceTracingDayRisk
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel
import io.kotest.matchers.shouldBe
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import java.time.Instant

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
        val ewRisk = AggregatedRiskPerDateResult(
            dateMillisSinceEpoch = Instant.parse("2021-03-22T14:00:00.000Z").toEpochMilli(),
            riskLevel = RiskLevel.HIGH,
            0,
            0
        )
        val ewRisk2 = AggregatedRiskPerDateResult(
            dateMillisSinceEpoch = Instant.parse("2021-03-19T14:00:00.000Z").toEpochMilli(),
            riskLevel = RiskLevel.LOW,
            0,
            0
        )
        val ewRisk3 = AggregatedRiskPerDateResult(
            dateMillisSinceEpoch = Instant.parse("2021-03-20T14:00:00.000Z").toEpochMilli(),
            riskLevel = RiskLevel.UNSPECIFIED,
            0,
            0
        )
        val ewRisk4 = AggregatedRiskPerDateResult(
            dateMillisSinceEpoch = Instant.parse("2021-03-15T14:00:00.000Z").toEpochMilli(),
            riskLevel = RiskLevel.UNSPECIFIED,
            0,
            0
        )

        val ptRiskList: List<PresenceTracingDayRisk> = listOf(ptRisk, ptRisk2, ptRisk3)
        val ewRiskList: List<AggregatedRiskPerDateResult> = listOf(ewRisk, ewRisk2, ewRisk3, ewRisk4)
        val result = combineRisk(ptRiskList, ewRiskList)
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
    fun `max RiskState works`() {
        max(RiskState.INCREASED_RISK, RiskState.INCREASED_RISK) shouldBe RiskState.INCREASED_RISK
        max(RiskState.INCREASED_RISK, RiskState.LOW_RISK) shouldBe RiskState.INCREASED_RISK
        max(RiskState.INCREASED_RISK, RiskState.CALCULATION_FAILED) shouldBe RiskState.INCREASED_RISK
        max(RiskState.LOW_RISK, RiskState.INCREASED_RISK) shouldBe RiskState.INCREASED_RISK
        max(RiskState.CALCULATION_FAILED, RiskState.INCREASED_RISK) shouldBe RiskState.INCREASED_RISK
        max(RiskState.LOW_RISK, RiskState.LOW_RISK) shouldBe RiskState.LOW_RISK
        max(RiskState.CALCULATION_FAILED, RiskState.LOW_RISK) shouldBe RiskState.LOW_RISK
        max(RiskState.CALCULATION_FAILED, RiskState.CALCULATION_FAILED) shouldBe RiskState.CALCULATION_FAILED
    }
}
