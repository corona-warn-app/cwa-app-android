package de.rki.coronawarnapp.datadonation.analytics.common

import org.joda.time.Days
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
