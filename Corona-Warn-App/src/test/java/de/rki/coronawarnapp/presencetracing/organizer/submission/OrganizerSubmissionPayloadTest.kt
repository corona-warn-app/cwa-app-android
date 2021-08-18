package de.rki.coronawarnapp.presencetracing.organizer.submission

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.ui.eventregistration.organizer.TraceLocationData
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class OrganizerSubmissionPayloadTest : BaseTest() {

    @Test
    fun toCheckIn() {
        val traceLocation = TraceLocation(
            id = 2,
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
            description = "Your Birthday Party",
            address = "at your place",
            startDate = 1618740005L.secondsToInstant(),
            endDate = 1618865545L.secondsToInstant(),
            defaultCheckInLengthInMinutes = null,
            cryptographicSeed = TraceLocationData.CRYPTOGRAPHIC_SEED.decodeBase64()!!,
            cnPublicKey = TraceLocationData.PUB_KEY,
            version = TraceLocation.VERSION
        )
        val organizerSubmissionPayload = OrganizerSubmissionPayload(
            traceLocation = traceLocation,
            startDate = Instant.parse("2021-05-10T11:35:00.000Z"),
            endDate = Instant.parse("2021-05-10T13:00:00.000Z"),
            tan = "TAN_TAN_TAN_TAN"
        )

        organizerSubmissionPayload.toCheckIn() shouldBe CheckIn(
            id = traceLocation.id,
            traceLocationId = traceLocation.locationId,
            version = traceLocation.version,
            type = traceLocation.type.number,
            description = traceLocation.description,
            address = traceLocation.address,
            traceLocationStart = traceLocation.startDate,
            traceLocationEnd = traceLocation.endDate,
            defaultCheckInLengthInMinutes = traceLocation.defaultCheckInLengthInMinutes,
            cryptographicSeed = traceLocation.cryptographicSeed,
            cnPublicKey = traceLocation.cnPublicKey,
            checkInStart = organizerSubmissionPayload.startDate,
            checkInEnd = organizerSubmissionPayload.endDate,
            completed = true,
            createJournalEntry = false,
            isSubmitted = false,
            hasSubmissionConsent = true
        )
    }
}
