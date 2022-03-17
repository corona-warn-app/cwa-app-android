package de.rki.coronawarnapp.datadonation.analytics.common

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CalculationsTest : BaseTest() {

    @Test
    fun `getLastChangeToHighRiskPt works`() {
        val ptRisk0 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 5).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk1 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 20).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK,
            calculatedFrom = LocalDate(2021, 3, 6).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk2 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 21).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 7).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk3 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK,
            calculatedFrom = LocalDate(2021, 3, 8).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk4 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.CALCULATION_FAILED,
            calculatedFrom = LocalDate(2021, 3, 9).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk5 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 24).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK,
            calculatedFrom = LocalDate(2021, 3, 10).toDateTimeAtStartOfDay().toInstant()
        )
        val registeredAt = LocalDate(2021, 3, 24).toDateTimeAtStartOfDay().toInstant()
        listOf(
            ptRisk0,
            ptRisk1,
            ptRisk2,
            ptRisk3,
            ptRisk4,
            ptRisk5
        ).getLastChangeToHighPtRiskBefore(registeredAt) shouldBe
            LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant()
    }

    @Test
    fun `getLastChangeToHighRiskPt works 2`() {
        val ptRisk0 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 5).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk1 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 20).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK,
            calculatedFrom = LocalDate(2021, 3, 6).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk2 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 21).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 7).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk3 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK,
            calculatedFrom = LocalDate(2021, 3, 8).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk4 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.CALCULATION_FAILED,
            calculatedFrom = LocalDate(2021, 3, 9).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk5 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 24).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK,
            calculatedFrom = LocalDate(2021, 3, 10).toDateTimeAtStartOfDay().toInstant()
        )
        val registeredAt = LocalDate(2021, 3, 21).toDateTimeAtStartOfDay().toInstant()
        listOf(
            ptRisk0,
            ptRisk1,
            ptRisk2,
            ptRisk3,
            ptRisk4,
            ptRisk5
        ).getLastChangeToHighPtRiskBefore(registeredAt) shouldBe
            LocalDate(2021, 3, 20).toDateTimeAtStartOfDay().toInstant()
    }

    @Test
    fun `getLastChangeToHighRiskPt works 3`() {
        val ptRisk0 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 5).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk1 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 20).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK,
            calculatedFrom = LocalDate(2021, 3, 6).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk2 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 21).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 7).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk3 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK,
            calculatedFrom = LocalDate(2021, 3, 8).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk4 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK,
            calculatedFrom = LocalDate(2021, 3, 9).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk5 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 24).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 10).toDateTimeAtStartOfDay().toInstant()
        )
        val registeredAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant()
        listOf(
            ptRisk0,
            ptRisk1,
            ptRisk2,
            ptRisk3,
            ptRisk4,
            ptRisk5
        ).getLastChangeToHighPtRiskBefore(registeredAt) shouldBe
            LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant()
    }

    @Test
    fun `getLastChangeToHighRiskPt works 4`() {
        val ptRisk0 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 5).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk1 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 20).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 6).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk2 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 21).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 7).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk3 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 8).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk4 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 9).toDateTimeAtStartOfDay().toInstant()
        )
        val ptRisk5 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 24).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 10).toDateTimeAtStartOfDay().toInstant()
        )
        val registeredAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant()
        listOf(
            ptRisk0,
            ptRisk1,
            ptRisk2,
            ptRisk3,
            ptRisk4,
            ptRisk5
        ).getLastChangeToHighPtRiskBefore(registeredAt) shouldBe
            null
    }

    @Test
    fun `getLastChangeToHighRiskPt works 5`() {
        val ptRisk0 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK,
            calculatedFrom = LocalDate(2021, 3, 5).toDateTimeAtStartOfDay().toInstant()
        )
        val registeredAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant()
        listOf(ptRisk0).getLastChangeToHighPtRiskBefore(registeredAt) shouldBe
            null
    }

    @Test
    fun `getLastChangeToHighRiskPt works 6`() {
        val registeredAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant()
        listOf<PtRiskLevelResult>().getLastChangeToHighPtRiskBefore(registeredAt) shouldBe
            null
    }

    @Test
    fun `getLastChangeToHighRiskPt works 7`() {
        val ptRisk0 = PtRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK,
            calculatedFrom = LocalDate(2021, 3, 5).toDateTimeAtStartOfDay().toInstant()
        )
        val registeredAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant()
        listOf(ptRisk0).getLastChangeToHighPtRiskBefore(registeredAt) shouldBe
            LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant()
    }

    @Test
    fun `getLastChangeToHighEwRiskBefore works`() {
        val ewRisk0 = TestEwRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val registeredAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant()
        listOf(ewRisk0).getLastChangeToHighEwRiskBefore(registeredAt) shouldBe
            LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant()
    }

    @Test
    fun `getLastChangeToHighEwRiskBefore works 2`() {
        val risk0 = TestEwRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 19).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK
        )
        val risk1 = TestEwRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 20).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val risk2 = TestEwRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 21).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.LOW_RISK
        )
        val risk3 = TestEwRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val risk4 = TestEwRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.CALCULATION_FAILED
        )
        val risk5 = TestEwRiskLevelResult(
            calculatedAt = LocalDate(2021, 3, 24).toDateTimeAtStartOfDay().toInstant(),
            riskState = RiskState.INCREASED_RISK
        )
        val registeredAt = LocalDate(2021, 3, 24).toDateTimeAtStartOfDay().toInstant()
        listOf(
            risk0,
            risk1,
            risk2,
            risk3,
            risk4,
            risk5
        ).getLastChangeToHighEwRiskBefore(registeredAt) shouldBe
            LocalDate(2021, 3, 22).toDateTimeAtStartOfDay().toInstant()
    }

    @Test
    fun `getLastChangeToHighEwRiskBefore works 3`() {
        val registeredAt = LocalDate(2021, 3, 23).toDateTimeAtStartOfDay().toInstant()
        listOf<EwRiskLevelResult>().getLastChangeToHighEwRiskBefore(registeredAt) shouldBe
            null
    }
}

private data class TestEwRiskLevelResult(
    override val calculatedAt: Instant,
    override val riskState: RiskState
) : EwRiskLevelResult {
    override val wasSuccessfullyCalculated = true
    override val failureReason: EwRiskLevelResult.FailureReason?
        get() = null
    override val ewAggregatedRiskResult: EwAggregatedRiskResult?
        get() = null
    override val exposureWindows: List<ExposureWindow>?
        get() = null
}
