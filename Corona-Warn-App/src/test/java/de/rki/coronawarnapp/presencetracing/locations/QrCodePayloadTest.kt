package de.rki.coronawarnapp.presencetracing.locations

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.qrCodePayload
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

class QrCodePayloadTest : BaseTest() {

    @Test
    fun `Trace location to QrCodePayload 1`() {
        val traceLocation = TraceLocation(
            id = 1,
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
            description = "My Birthday Party",
            address = "at my place",
            startDate = Instant.ofEpochSecond(2687955L),
            endDate = Instant.ofEpochSecond(2687991L),
            defaultCheckInLengthInMinutes = null,
            cryptographicSeed = CRYPTOGRAPHIC_SEED.decodeBase64()!!,
            cnPublicKey = PUB_KEY,
            version = TraceLocation.VERSION
        )

        traceLocation.qrCodePayload() shouldBe
            TraceLocationOuterClass.QRCodePayload.parseFrom(PAYLOAD_1.decodeBase64()!!.toByteArray())
    }

    @Test
    fun `Trace location to QrCodePayload 2`() {
        val traceLocation = TraceLocation(
            id = 2,
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER,
            description = "Icecream Shop",
            address = "Main Street 1",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = 10,
            cryptographicSeed = CRYPTOGRAPHIC_SEED.decodeBase64()!!,
            cnPublicKey = PUB_KEY,
            version = TraceLocation.VERSION
        )

        traceLocation.qrCodePayload() shouldBe
            TraceLocationOuterClass.QRCodePayload.parseFrom(PAYLOAD_2.decodeBase64()!!.toByteArray())
    }

    companion object {
        private const val PAYLOAD_1 =
            "CAESLAgBEhFNeSBCaXJ0aGRheSBQYXJ0eRoLYXQgbXkgcGxhY2Uo04ekATD3h6QBGmUIARJbMFkwEwYHKoZIzj0CAQYIKo" +
                "ZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxe" +
                "uFMZAIX2+6A5XhoEMTIzNCIECAEQAg=="

        private const val PAYLOAD_2 =
            "CAESIAgBEg1JY2VjcmVhbSBTaG9wGg1NYWluIFN0cmVldCAxGmUIARJbMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIR" +
                "cyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5XhoEMTIzNCIGCAEQARgK"

        private const val CRYPTOGRAPHIC_SEED = "MTIzNA=="
        private const val PUB_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0z" +
                "K7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg=="
    }
}
