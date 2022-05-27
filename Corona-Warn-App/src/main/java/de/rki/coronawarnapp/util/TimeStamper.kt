package de.rki.coronawarnapp.util

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeStamper @Inject constructor() {

    val nowJavaUTC: java.time.Instant
        get() = java.time.Instant.now()
}
