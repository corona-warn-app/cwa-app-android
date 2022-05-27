package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDateTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun BaseCoronaTest.isOlderThan21Days(nowJavaUTC: Instant): Boolean {
    return registeredAt.toDateTime().plusDays(21)
        .isBefore(LocalDateTime.ofInstant(nowJavaUTC, ZoneOffset.systemDefault()))
}
