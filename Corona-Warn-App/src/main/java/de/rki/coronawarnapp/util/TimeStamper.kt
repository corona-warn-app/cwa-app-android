package de.rki.coronawarnapp.util

import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeStamper @Inject constructor() {

    val nowUTC: Instant
        get() = Instant.now()

}
