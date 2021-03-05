package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.server.protocols.internal.evreg.SignedEventOuterClass
import org.joda.time.Instant

data class DefaultHostedEvent(
    override val guid: String,
    override val description: String,
    override val location: String,
    override val startTime: Instant?,
    override val endTime: Instant?,
    override val defaultCheckInLengthInMinutes: Int?,
    override val signature: String
) : HostedEvent

fun SignedEventOuterClass.SignedEvent.toHostedEvent(): HostedEvent =
    DefaultHostedEvent(
        guid = event.guid.toString(),
        description = event.description,
        location = "hardcodedLocation", // event.location,
        // backend needs UNIX timestamp in seconds, so we have to multiply it by 1000 to get milliseconds
        startTime = Instant.ofEpochMilli(event.start.toLong() * 1000),
        endTime = Instant.ofEpochMilli(event.end.toLong() * 1000),
        defaultCheckInLengthInMinutes = event.defaultCheckInLengthInMinutes,
        signature = signature.toString()
    )
