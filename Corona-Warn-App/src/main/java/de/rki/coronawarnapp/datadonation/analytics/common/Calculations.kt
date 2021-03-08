package de.rki.coronawarnapp.datadonation.analytics.common

import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import org.joda.time.Days
import org.joda.time.Instant

fun calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
    lastChangeCheckedRiskLevelTimestamp: Instant?,
    testRegisteredAt: Instant?
): Int {
    val lastChangeCheckedRiskLevelDate = lastChangeCheckedRiskLevelTimestamp?.toLocalDate() ?: return 0
    val testRegisteredAtDate = testRegisteredAt?.toLocalDate() ?: return 0
    return Days.daysBetween(
        lastChangeCheckedRiskLevelDate,
        testRegisteredAtDate
    ).days
}
