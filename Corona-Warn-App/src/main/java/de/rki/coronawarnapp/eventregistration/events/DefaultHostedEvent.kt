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
        startTime = Instant.ofEpochMilli(event.start.toLong()),
        endTime = Instant.ofEpochMilli(event.end.toLong()),
        defaultCheckInLengthInMinutes = event.defaultCheckInLengthInMinutes,
        signature = signature.toString()
    )
