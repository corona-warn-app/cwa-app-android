package de.rki.coronawarnapp.eventregistration.events.server

import com.google.protobuf.ByteString
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

    val signedTraceLocationTemporary: TraceLocationOuterClass.SignedTraceLocation =
        TraceLocationOuterClass.SignedTraceLocation.newBuilder()
            .setLocation(traceLocationTemporary)
            .setSignature(ByteString.copyFromUtf8("ServerSignature"))
            .build()
}
