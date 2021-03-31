package de.rki.coronawarnapp.eventregistration.events.server

import de.rki.coronawarnapp.eventregistration.events.TraceLocationUserInput
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import org.joda.time.Instant

object TraceLocationData {

    val traceLocationTemporaryUserInput = TraceLocationUserInput(
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
        description = "Event Registration Release Party",
        address = "SAP Headquarter",
        startDate = Instant.parse("2021-05-01T19:00:00.000Z"),
        endDate = Instant.parse("2021-05-01T23:30:00.000Z"),
        defaultCheckInLengthInMinutes = 180
    )

    private val traceLocationTemporary: TraceLocationOuterClass.TraceLocation =
        TraceLocationOuterClass.TraceLocation.newBuilder()
            .setGuid("serverGeneratedGuid")
            .setVersion(1)
            .setType(TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER)
            .setDescription("Event Registration Release Party")
            .setAddress("SAP Headquarter")
            .setStartTimestamp(Instant.parse("2021-05-01T19:00:00.000Z").seconds)
            .setEndTimestamp(Instant.parse("2021-05-01T23:30:00.000Z").seconds)
            .setDefaultCheckInLengthInMinutes(180)
            .build()

    private val traceLocationPermanent: TraceLocationOuterClass.TraceLocation =
        TraceLocationOuterClass.TraceLocation.newBuilder()
            .setGuid("serverGeneratedGuid")
            .setVersion(1)
            .setType(TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER)
            .setDescription("IceCream Shop")
            .setAddress("IceCream Wonderland Street 1")
            .setDefaultCheckInLengthInMinutes(30)
            .build()
}
