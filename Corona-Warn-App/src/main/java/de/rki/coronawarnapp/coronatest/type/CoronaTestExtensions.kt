package de.rki.coronawarnapp.coronatest.type

import org.joda.time.Instant

fun BaseCoronaTest.isOlderThan21Days(nowUTC: Instant): Boolean {
    return registeredAt.toDateTime().plusDays(21).isBefore(nowUTC)
}
