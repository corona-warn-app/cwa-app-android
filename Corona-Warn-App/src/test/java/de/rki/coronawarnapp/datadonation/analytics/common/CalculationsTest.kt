package de.rki.coronawarnapp.datadonation.analytics.common

import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import io.kotest.matchers.shouldBe
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CalculationsTest : BaseTest() {

    @Test
    fun `getLastChangeToHighRiskPt works`() {
        val ptRisk0 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK
        )
        val ptRisk1 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 20).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val ptRisk2 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 21).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK
        )
        val ptRisk3 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val ptRisk4 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.CALCULATION_FAILED
        )
        val ptRisk5 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 24).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val registeredAt = LocalDate(2021, 3, 24).toDateTimeAtStartOfDay().toInstant()
        listOf(ptRisk0, ptRisk1, ptRisk2, ptRisk3, ptRisk4, ptRisk5).getLastChangeToHighRiskPt(registeredAt) shouldBe
            LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant()
    }

    @Test
    fun `getLastChangeToHighRiskPt works 2`() {
        val ptRisk0 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK
        )
        val ptRisk1 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 20).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val ptRisk2 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 21).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK
        )
        val ptRisk3 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val ptRisk4 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.CALCULATION_FAILED
        )
        val ptRisk5 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 24).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val registeredAt = LocalDate(2021, 3, 21).toDateTimeAtStartOfDay().toInstant()
        listOf(ptRisk0, ptRisk1, ptRisk2, ptRisk3, ptRisk4, ptRisk5).getLastChangeToHighRiskPt(registeredAt) shouldBe
            LocalDate(2021, 3, 20).toDateTimeAtStartOfDay().toInstant()
    }

    @Test
    fun `getLastChangeToHighRiskPt works 3`() {
        val ptRisk0 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK
        )
        val ptRisk1 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 20).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val ptRisk2 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 21).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK
        )
        val ptRisk3 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val ptRisk4 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val ptRisk5 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 24).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK
        )
        val registeredAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant()
        listOf(ptRisk0, ptRisk1, ptRisk2, ptRisk3, ptRisk4, ptRisk5).getLastChangeToHighRiskPt(registeredAt) shouldBe
            LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant()
    }
}
