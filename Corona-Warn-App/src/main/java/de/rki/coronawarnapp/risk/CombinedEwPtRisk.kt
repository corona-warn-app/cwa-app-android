package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.storage.max
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import org.joda.time.Instant
import org.joda.time.LocalDate

data class CombinedEwPtDayRisk(
    val localDate: LocalDate,
    val riskState: RiskState
)

data class CombinedEwPtRiskLevelResult(
    val ptRiskLevelResult: PtRiskLevelResult,
    val ewRiskLevelResult: EwRiskLevelResult
) {

    val riskState: RiskState by lazy {
        max(ptRiskLevelResult.riskState, ewRiskLevelResult.riskState)
    }

    val wasSuccessfullyCalculated: Boolean by lazy {
        riskState != RiskState.CALCULATION_FAILED
    }

    val calculatedAt: Instant by lazy {
        max(ewRiskLevelResult.calculatedAt, ptRiskLevelResult.calculatedAt)
    }

    val daysWithEncounters: Int by lazy {
        when (riskState) {
            RiskState.INCREASED_RISK -> {
                (ewRiskLevelResult.ewAggregatedRiskResult?.numberOfDaysWithHighRisk ?: 0) +
                    ptRiskLevelResult.numberOfDaysWithHighRisk
            }
            RiskState.LOW_RISK -> {
                (ewRiskLevelResult.ewAggregatedRiskResult?.numberOfDaysWithLowRisk ?: 0) +
                    ptRiskLevelResult.numberOfDaysWithLowRisk
            }
            else -> 0
        }
    }

    val lastRiskEncounterAt: LocalDate? by lazy {
        when (riskState) {
            RiskState.INCREASED_RISK -> max(
                ewRiskLevelResult.ewAggregatedRiskResult?.mostRecentDateWithHighRisk?.toLocalDateUtc(),
                ptRiskLevelResult.mostRecentDateWithHighRisk
            )
            RiskState.LOW_RISK -> max(
                ewRiskLevelResult.ewAggregatedRiskResult?.mostRecentDateWithLowRisk?.toLocalDateUtc(),
                ptRiskLevelResult.mostRecentDateWithLowRisk
            )
            else -> null
        }
    }
}

data class LastCombinedRiskResults(
    val lastCalculated: CombinedEwPtRiskLevelResult,
    val lastSuccessfullyCalculated: CombinedEwPtRiskLevelResult
)
