package de.rki.coronawarnapp.eventregistration.storage

import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationEntity
import org.joda.time.Instant

object TraceLocationDatabaseData {

    val testTraceLocation1 = TraceLocationEntity(
        guid = "TestGuid1",
        version = 1,
        type = TraceLocation.Type.TEMPORARY_OTHER,
        description = "TestTraceLocation1",
        address = "TestTraceLocationAddress1",
        startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
        endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
        defaultCheckInLengthInMinutes = null,
        signature = "signature1"
    )

    val testTraceLocation2 = TraceLocationEntity(
        guid = "TestGuid2",
        version = 1,
        type = TraceLocation.Type.PERMANENT_OTHER,
        description = "TestTraceLocation2",
        address = "TestTraceLocationAddress2",
        startDate = null,
        endDate = null,
        defaultCheckInLengthInMinutes = 15,
        signature = "signature2"
    )
}
