package de.rki.coronawarnapp.datadonation.analytics.common

import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
    lastDateAtRiskLevel: LocalDate?,
    testRegisteredAt: LocalDate?
): Int {
    testRegisteredAt ?: return -1
    lastDateAtRiskLevel ?: return -1
    if (lastDateAtRiskLevel.isAfter(testRegisteredAt)) return -1
    return ChronoUnit.DAYS.between(lastDateAtRiskLevel, testRegisteredAt).toInt()
}

fun List<PtRiskLevelResult>.getLastChangeToHighPtRiskBefore(testRegisteredAt: Instant): Instant? {
    val successfulResults = filter { it.wasSuccessfullyCalculated }
        .filter { it.calculatedAt <= testRegisteredAt }
        .sortedByDescending { it.calculatedAt }

    successfulResults.forEachIndexed { index, ptRiskLevelResult ->
        if (ptRiskLevelResult.riskState == RiskState.INCREASED_RISK &&
            (index == successfulResults.lastIndex || successfulResults[index + 1].riskState == RiskState.LOW_RISK)
        ) {
            return ptRiskLevelResult.calculatedAt
        }
    }
    return null
}

fun List<EwRiskLevelResult>.getLastChangeToHighEwRiskBefore(testRegisteredAt: Instant): Instant? {
    val successfulResults = filter { it.wasSuccessfullyCalculated }
        .filter { it.calculatedAt <= testRegisteredAt }
        .sortedByDescending { it.calculatedAt }

    successfulResults.forEachIndexed { index, ewRiskLevelResult ->
        if (ewRiskLevelResult.riskState == RiskState.INCREASED_RISK &&
            (index == successfulResults.lastIndex || successfulResults[index + 1].riskState == RiskState.LOW_RISK)
        ) {
            return ewRiskLevelResult.calculatedAt
        }
    }
    return null
}
