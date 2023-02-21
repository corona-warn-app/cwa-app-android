package de.rki.coronawarnapp.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeStamper @Inject constructor() {

    val nowUTC: Instant get() = Instant.now()
    val nowZonedDateTime: ZonedDateTime get() = ZonedDateTime.now(ZoneId.of("CET"))
}
