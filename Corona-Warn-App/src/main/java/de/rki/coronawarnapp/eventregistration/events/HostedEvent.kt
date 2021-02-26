package de.rki.coronawarnapp.eventregistration.events

import org.joda.time.Duration
import org.joda.time.Instant

/**
 * A verified event that the host has been registered.
 * If you are a host, this is the event you created successfully.
 */
interface HostedEvent {

    val guid: String
    val description: String
    val startTime: Instant?
    val endTime: Instant?
    val defaultCheckInLength: Duration?
    val signature: String
}
