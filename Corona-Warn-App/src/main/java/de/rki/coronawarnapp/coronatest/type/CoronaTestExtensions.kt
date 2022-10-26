package de.rki.coronawarnapp.coronatest.type

import java.time.Instant
import java.time.ZoneId

fun BaseCoronaTest.isOlderThan21Days(nowUTC: Instant): Boolean {
    return registeredAt
        .atZone(ZoneId.systemDefault())
        .plusDays(21)
        .toInstant()
        .isBefore(nowUTC)
}
