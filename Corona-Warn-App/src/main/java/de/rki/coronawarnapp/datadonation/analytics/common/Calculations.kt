package de.rki.coronawarnapp.datadonation.analytics.common

import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.LocalDate

fun calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
    lastDateAtRiskLevel: LocalDate?,
    testRegisteredAt: LocalDate?
): Int {
    testRegisteredAt ?: return -1
    lastDateAtRiskLevel ?: return -1
    if (lastDateAtRiskLevel.isAfter(testRegisteredAt)) return -1
    return Days.daysBetween(
        lastDateAtRiskLevel,
        testRegisteredAt
    ).days
}

fun List<PtRiskLevelResult>.getLastChangeToHighRiskPt(testRegisteredAt: Instant): Instant? {
    val relevantResults = filter { it.wasSuccessfullyCalculated }
        .filter { it.calculatedAt <= testRegisteredAt }
        .sortedByDescending { it.calculatedAt }

    relevantResults.forEachIndexed { index, ptRiskLevelResult ->
        if (ptRiskLevelResult.riskState == RiskState.INCREASED_RISK &&
            (index == relevantResults.lastIndex || relevantResults[index + 1].riskState == RiskState.LOW_RISK)
        ) {
            return ptRiskLevelResult.calculatedAt
        }
    }
    return null
}

fun List<EwRiskLevelResult>.getLastChangeToHighRiskEw(testRegisteredAt: Instant): Instant? {
    val relevantResults = filter { it.wasSuccessfullyCalculated }
        .filter { it.calculatedAt <= testRegisteredAt }
        .sortedByDescending { it.calculatedAt }

    relevantResults.forEachIndexed { index, ptRiskLevelResult ->
        if (ptRiskLevelResult.riskState == RiskState.INCREASED_RISK &&
            (index == relevantResults.lastIndex || relevantResults[index + 1].riskState == RiskState.LOW_RISK)
        ) {
            return ptRiskLevelResult.calculatedAt
        }
    }
    return null
}
