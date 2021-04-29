package de.rki.coronawarnapp.coronatest.type

import org.joda.time.Duration
import org.joda.time.Instant

fun CoronaTest.isOlderThan21Days(nowUTC: Instant): Boolean {
    return Duration(registeredAt, nowUTC).standardDays > 21
}
