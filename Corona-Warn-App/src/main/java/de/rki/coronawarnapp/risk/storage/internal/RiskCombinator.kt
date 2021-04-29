package de.rki.coronawarnapp.risk.storage.internal

import dagger.Reusable
import de.rki.coronawarnapp.presencetracing.risk.EwRiskCalcResult
import de.rki.coronawarnapp.presencetracing.risk.PtRiskCalcResult
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.risk.CombinedEwPtDayRisk
import de.rki.coronawarnapp.risk.CombinedEwPtRiskCalcResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.mapToRiskState
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class RiskCombinator @Inject constructor(
    private val timeStamper: TimeStamper
) {

    private val initialEwRiskCalcResult: EwRiskCalcResult = EwRiskCalcResult(
        calculatedAt = Instant.EPOCH,
        riskState = RiskState.LOW_RISK
    )

    private val initialPtRiskCalcResult: PtRiskCalcResult = PtRiskCalcResult(
        calculatedAt = Instant.EPOCH,
        riskState = RiskState.LOW_RISK
    )

    internal val initialCombinedResult = CombinedEwPtRiskCalcResult(
        ptRiskCalcResult = initialPtRiskCalcResult,
        ewRiskCalcResult = initialEwRiskCalcResult
    )

    private val ewCurrentLowRiskLevelResult: EwRiskCalcResult
        get() = EwRiskCalcResult(
            calculatedAt = timeStamper.nowUTC,
            riskState = RiskState.LOW_RISK
        )

    private val ptCurrentLowRiskCalcResult: PtRiskCalcResult
        get() = PtRiskCalcResult(
            calculatedAt = timeStamper.nowUTC,
            riskState = RiskState.LOW_RISK
        )

    internal val latestCombinedResult: CombinedEwPtRiskCalcResult
        get() = CombinedEwPtRiskCalcResult(
            ptCurrentLowRiskCalcResult,
            ewCurrentLowRiskLevelResult
        )

    internal fun combineEwPtRiskLevelResults(
        ptRiskResults: List<PtRiskCalcResult>,
        ewRiskResults: List<EwRiskCalcResult>
    ): List<CombinedEwPtRiskCalcResult> {
        val allDates = ptRiskResults.map { it.calculatedAt }.plus(ewRiskResults.map { it.calculatedAt }).distinct()
        val sortedPtResults = ptRiskResults.sortedByDescending { it.calculatedAt }
        val sortedEwResults = ewRiskResults.sortedByDescending { it.calculatedAt }
        return allDates.map { date ->
            val ptRisk = sortedPtResults.find {
                it.calculatedAt <= date
            } ?: initialPtRiskCalcResult
            val ewRisk = sortedEwResults.find {
                it.calculatedAt <= date
            } ?: initialEwRiskCalcResult

            CombinedEwPtRiskCalcResult(
                ptRiskCalcResult = ptRisk,
                ewRiskCalcResult = ewRisk
            )
        }
    }

    internal fun combineRisk(
        ptRiskList: List<PresenceTracingDayRisk>,
        exposureWindowDayRiskList: List<ExposureWindowDayRisk>
    ): List<CombinedEwPtDayRisk> {
        val allDates =
            ptRiskList.map { it.localDateUtc }.plus(exposureWindowDayRiskList.map { it.localDateUtc }).distinct()
        return allDates.map { date ->
            val ptRisk = ptRiskList.find { it.localDateUtc == date }
            val ewRisk = exposureWindowDayRiskList.find { it.localDateUtc == date }
            CombinedEwPtDayRisk(
                localDate = date,
                riskState = combine(
                    ptRisk?.riskState,
                    ewRisk?.riskLevel?.mapToRiskState()
                )
            )
        }
    }

    companion object {
        fun combine(vararg states: RiskState?): RiskState {
            if (states.any { it == RiskState.CALCULATION_FAILED }) return RiskState.CALCULATION_FAILED
            if (states.any { it == RiskState.INCREASED_RISK }) return RiskState.INCREASED_RISK

            require(states.filterNotNull().all { it == RiskState.LOW_RISK })

            return RiskState.LOW_RISK
        }
    }
}
