package de.rki.coronawarnapp.eventregistration.storage

import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import org.joda.time.Instant

object CheckInDatabaseData {

    val testCheckIn = TraceLocationCheckInEntity(
        guid = "testGuid1",
        guidHash = byteArrayOf(),
        version = 1,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER.number,
        description = "testDescription1",
        address = "testAddress1",
        traceLocationStart = Instant.parse("2021-01-01T12:00:00.000Z"),
        traceLocationEnd = Instant.parse("2021-01-01T15:00:00.000Z"),
        defaultCheckInLengthInMinutes = 15,
        traceLocationBytes = byteArrayOf(),
        signatureBase64 = "Signature",
        checkInStart = Instant.parse("2021-01-01T12:30:00.000Z"),
        checkInEnd = Instant.parse("2021-01-01T14:00:00.000Z"),
        completed = false,
        createJournalEntry = true
    )

    val testCheckInWithoutCheckOutTime = TraceLocationCheckInEntity(
        guid = "testGuid2",
        guidHash = byteArrayOf(),
        version = 1,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER.number,
        description = "testDescription2",
        address = "testAddress2",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        traceLocationBytes = byteArrayOf(),
        signatureBase64 = "Signature",
        checkInStart = Instant.parse("2021-01-01T12:30:00.000Z"),
        checkInEnd = Instant.parse("2021-01-01T14:00:00.000Z"),
        completed = false,
        createJournalEntry = true
    )
}
