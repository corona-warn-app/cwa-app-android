package de.rki.coronawarnapp.datadonation.analytics.common

import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import org.joda.time.Days
import org.joda.time.Instant

fun calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
    lastChangeCheckedRiskLevelTimestamp: Instant?,
    testRegisteredAt: Instant?
): Int {
    val lastChangeCheckedRiskLevelDate = lastChangeCheckedRiskLevelTimestamp?.toLocalDateUtc() ?: return -1
    val testRegisteredAtDate = testRegisteredAt?.toLocalDateUtc() ?: return -1
    if (lastChangeCheckedRiskLevelDate.isAfter(testRegisteredAtDate)) return -1
    return Days.daysBetween(
        lastChangeCheckedRiskLevelDate,
        testRegisteredAtDate
    ).days
}
