package de.rki.coronawarnapp.presencetracing.organizer.submission

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import java.time.Instant
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
            cryptographicSeed = CRYPTOGRAPHIC_SEED.decodeBase64()!!,
            cnPublicKey = PUB_KEY,
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

    companion object {
        const val CRYPTOGRAPHIC_SEED = "MTIzNA=="

        const val PUB_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0z" +
                "K7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg=="
    }
}
