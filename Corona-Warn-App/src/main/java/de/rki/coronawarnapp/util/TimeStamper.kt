package de.rki.coronawarnapp.util

import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeStamper @Inject constructor() {

    @Deprecated("Use java time") //, ReplaceWith("nowJavaUTC"))
    val nowUTC: Instant
        get() = Instant.now()

    val nowJavaUTC: java.time.Instant
        get() = java.time.Instant.now()
}
