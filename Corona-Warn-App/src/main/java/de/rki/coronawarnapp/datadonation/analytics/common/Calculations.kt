package de.rki.coronawarnapp.datadonation.analytics.common

import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.LocalDate

fun calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
    lastDateAtRiskLevel: Instant?,
    testRegisteredAt: Instant?
): Int {
    val lastChangeCheckedRiskLevelDate = lastDateAtRiskLevel?.toLocalDateUtc() ?: return -1
    val testRegisteredAtDate = testRegisteredAt?.toLocalDateUtc() ?: return -1
    if (lastChangeCheckedRiskLevelDate.isAfter(testRegisteredAtDate)) return -1
    return Days.daysBetween(
        lastChangeCheckedRiskLevelDate,
        testRegisteredAtDate
    ).days
}

fun calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
    lastDateAtRiskLevel: LocalDate?,
    testRegisteredAt: Instant?
): Int {
    val testRegisteredAtDate = testRegisteredAt?.toLocalDateUtc() ?: return -1
    lastDateAtRiskLevel ?: return -1
    if (lastDateAtRiskLevel.isAfter(testRegisteredAtDate)) return -1
    return Days.daysBetween(
        lastDateAtRiskLevel,
        testRegisteredAtDate
    ).days
}
