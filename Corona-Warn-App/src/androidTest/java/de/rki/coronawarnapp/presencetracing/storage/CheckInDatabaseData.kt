package de.rki.coronawarnapp.presencetracing.storage

import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import okio.ByteString.Companion.encode
import java.time.Instant

object CheckInDatabaseData {

    val testCheckIn = TraceLocationCheckInEntity(
        traceLocationIdBase64 = "traceLocationId1".encode().base64(),
        version = 1,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER.number,
        description = "testDescription1",
        address = "testAddress1",
        traceLocationStart = Instant.parse("2021-01-01T12:00:00.000Z"),
        traceLocationEnd = Instant.parse("2021-01-01T15:00:00.000Z"),
        defaultCheckInLengthInMinutes = 15,
        cryptographicSeedBase64 = "cryptographicSeed".encode().base64(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.parse("2021-01-01T12:30:00.000Z"),
        checkInEnd = Instant.parse("2021-01-01T14:00:00.000Z"),
        completed = false,
        createJournalEntry = true,
        isSubmitted = false,
        hasSubmissionConsent = false,
    )

    val testCheckInWithoutCheckOutTime = TraceLocationCheckInEntity(
        traceLocationIdBase64 = "traceLocationId1".encode().base64(),
        version = 1,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER.number,
        description = "testDescription2",
        address = "testAddress2",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        cryptographicSeedBase64 = "cryptographicSeed".encode().base64(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.parse("2021-01-01T12:30:00.000Z"),
        checkInEnd = Instant.parse("2021-01-01T14:00:00.000Z"),
        completed = false,
        createJournalEntry = true,
        isSubmitted = false,
        hasSubmissionConsent = false,
    )
}
