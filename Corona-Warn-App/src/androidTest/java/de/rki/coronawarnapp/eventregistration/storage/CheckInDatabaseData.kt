package de.rki.coronawarnapp.eventregistration.storage

import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import org.joda.time.Instant

object CheckInDatabaseData {

    val testCheckIn = TraceLocationCheckInEntity(
        guid = "testGuid1",
        version = 1,
        type = TraceLocation.Type.TEMPORARY_OTHER.value,
        description = "testDescription1",
        address = "testAddress1",
        traceLocationStart = Instant.parse("2021-01-01T12:00:00.000Z"),
        traceLocationEnd = Instant.parse("2021-01-01T15:00:00.000Z"),
        defaultCheckInLengthInMinutes = 15,
        signature = "Signature",
        checkInStart = Instant.parse("2021-01-01T12:30:00.000Z"),
        checkInEnd = Instant.parse("2021-01-01T14:00:00.000Z"),
        targetCheckInEnd = Instant.parse("2021-01-01T12:45:00.000Z"),
        createJournalEntry = true
    )

    val testCheckInWithoutCheckOutTime = TraceLocationCheckInEntity(
        guid = "testGuid2",
        version = 1,
        type = TraceLocation.Type.TEMPORARY_OTHER.value,
        description = "testDescription2",
        address = "testAddress2",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        signature = "Signature",
        checkInStart = Instant.parse("2021-01-01T12:30:00.000Z"),
        checkInEnd = null,
        targetCheckInEnd = Instant.parse("2021-01-01T12:45:00.000Z"),
        createJournalEntry = true
    )
}
