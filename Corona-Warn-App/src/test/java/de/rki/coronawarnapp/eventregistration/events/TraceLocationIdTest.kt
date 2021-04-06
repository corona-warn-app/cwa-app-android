package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocationId
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.toTraceLocationIdHash
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.traceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import io.kotest.matchers.shouldBe
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TraceLocationIdTest : BaseTest() {
    @Test
    fun `locationId from qrCodePayloadBase64 - 1`() {
        val qrCodePayloadBase64 =
            "CAESLAgBEhFNeSBCaXJ0aGRheSBQYXJ0eRoLYXQgbXkgcGxhY2Uo04ekATD3h6QBGmUIARJbMFkwEwYHKoZIzj0CAQYIKo" +
                "ZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxe" +
                "uFMZAIX2+6A5XhoEMTIzNCIECAEQAg=="
        val qrCodePayload = TraceLocationOuterClass.QRCodePayload.parseFrom(
            qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        )
        qrCodePayload.traceLocation().locationId.base64() shouldBe "jNcJTCajd9Sen6Tbexl2Yb7O3J7ps47b6k4+QMT4xS0="
    }

    @Test
    fun `locationId from qrCodePayloadBase64 - 2`() {
        val qrCodePayloadBase64 =
            "CAESIAgBEg1JY2VjcmVhbSBTaG9wGg1NYWluIFN0cmVldCAxGmUIARJbMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIR" +
                "cyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5XhoEMTIzNCIGCAEQARgK"
        val qrCodePayload = TraceLocationOuterClass.QRCodePayload.parseFrom(
            qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        )
        qrCodePayload.traceLocation().locationId.base64() shouldBe "GMuCjqNmOdYyrFhyvFNTVEeLaZh+uShgUoY0LYJo4YQ="
    }

    @Test
    fun `locationId from traceLocation - 1`() {
        val traceLocation = TraceLocation(
            id = 1,
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
            description = "My Birthday Party",
            address = "at my place",
            startDate = 2687955L.secondsToInstant(),
            endDate = 2687991L.secondsToInstant(),
            defaultCheckInLengthInMinutes = null,
            cryptographicSeed = CRYPTOGRAPHIC_SEED.decodeBase64()!!,
            cnPublicKey = PUB_KEY,
            version = TraceLocation.VERSION
        )
        traceLocation.locationId.base64() shouldBe "jNcJTCajd9Sen6Tbexl2Yb7O3J7ps47b6k4+QMT4xS0="
    }

    @Test
    fun `locationId from traceLocation - 2`() {
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

        traceLocation.locationId.base64() shouldBe "GMuCjqNmOdYyrFhyvFNTVEeLaZh+uShgUoY0LYJo4YQ="
    }

    /**
     * Match server calculation
     * https://github.com/corona-warn-app/cwa-server/blob/5ce7d27a74fbf4f2ed560772f97ac17e2189ad33/common/persistence/src/test/java/app/coronawarn/server/common/persistence/service/TraceTimeIntervalWarningServiceTest.java#L141
     */
    @Test
    fun `test tracelocation hash generation`() {
        val locationId = "afa27b44d43b02a9fea41d13cedc2e4016cfcf87c5dbf990e593669aa8ce286d"
        val locationIdByte: ByteString = locationId.decodeHex()
        val hashedLocationId: ByteString = locationIdByte.sha256()

        val expectedLocationIdHash = "0f37dac11d1b8118ea0b44303400faa5e3b876da9d758058b5ff7dc2e5da8230"

        hashedLocationId.hex() shouldBe expectedLocationIdHash
        hashedLocationId shouldBe expectedLocationIdHash.decodeHex()
    }

    @Test
    fun `turn location ID into locationID hash`() {
        val traceLocationIdHex = "afa27b44d43b02a9fea41d13cedc2e4016cfcf87c5dbf990e593669aa8ce286d"
        val expectedLocationIdHashHex = "0f37dac11d1b8118ea0b44303400faa5e3b876da9d758058b5ff7dc2e5da8230"

        val traceLocationId: TraceLocationId = traceLocationIdHex.decodeHex()
        traceLocationId.toTraceLocationIdHash() shouldBe expectedLocationIdHashHex.decodeHex()
    }

    companion object {
        private const val CRYPTOGRAPHIC_SEED = "MTIzNA=="
        private const val PUB_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0z" +
                "K7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg=="
    }
}
