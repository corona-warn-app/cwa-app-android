package de.rki.coronawarnapp.presencetracing.locations

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationId
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.qrCodePayload
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.toTraceLocationIdHash
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.traceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import io.kotest.matchers.shouldBe
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import java.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class TraceLocationIdTest : BaseTest() {
    @Test
    fun `locationId from qrCodePayloadBase64 - 1`() {
        val qrCodePayloadBase64 =
            "CAESLAgBEhFNeSBCaXJ0aGRheSBQYXJ0eRoLYXQgbXkgcGxhY2Uo04ekATD3h6QBGmUIARJbMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5XhoEMTIzNCIECAEQAg=="
        val qrCodePayload = TraceLocationOuterClass.QRCodePayload.parseFrom(
            qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        )
        qrCodePayload.traceLocation().apply {
            locationId.base64() shouldBe "jNcJTCajd9Sen6Tbexl2Yb7O3J7ps47b6k4+QMT4xS0="
        }
    }

    @Test
    fun `locationId from qrCodePayloadBase64 - 2`() {
        val qrCodePayloadBase64 =
            "CAESIAgBEg1JY2VjcmVhbSBTaG9wGg1NYWluIFN0cmVldCAxGmUIARJbMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5XhoEMTIzNCIGCAEQARgK"
        val qrCodePayload = TraceLocationOuterClass.QRCodePayload.parseFrom(
            qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        )
        qrCodePayload.traceLocation().apply {
            locationId.base64() shouldBe "GMuCjqNmOdYyrFhyvFNTVEeLaZh+uShgUoY0LYJo4YQ="
        }
    }

    @Test
    fun `locationId from traceLocation - 1`() {
        val traceLocation = TraceLocation(
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
            description = "My Birthday Party",
            address = "at my place",
            startDate = 2687955L.secondsToInstant(),
            endDate = 2687991L.secondsToInstant(),
            defaultCheckInLengthInMinutes = null,
            cryptographicSeed = "MTIzNA==".decodeBase64()!!,
            cnPublicKey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg==",
            version = TraceLocation.VERSION
        )
        traceLocation.locationId.base64() shouldBe "jNcJTCajd9Sen6Tbexl2Yb7O3J7ps47b6k4+QMT4xS0="
    }

    @Test
    fun `locationId from traceLocation - 2`() {
        val traceLocation = TraceLocation(
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER,
            description = "Icecream Shop",
            address = "Main Street 1",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = 10,
            cryptographicSeed = "MTIzNA==".decodeBase64()!!,
            cnPublicKey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg==",
            version = TraceLocation.VERSION
        )

        traceLocation.locationId.base64() shouldBe "GMuCjqNmOdYyrFhyvFNTVEeLaZh+uShgUoY0LYJo4YQ="
    }

    /**
     * Match server calculation
     * https://github.com/corona-warn-app/cwa-server/blob/5ce7d27a74fbf4f2ed560772f97ac17e2189ad33/common/persistence/src/test/java/app/coronawarn/server/common/persistence/service/TraceTimeIntervalWarningServiceTest.java#L141
     */
    @Test
    fun `test trace location hash generation`() {
        val locationId = "afa27b44d43b02a9fea41d13cedc2e4016cfcf87c5dbf990e593669aa8ce286d"
        val locationIdByte: ByteString = locationId.decodeHex()
        val hashedLocationId: ByteString = locationIdByte.sha256()

        val expectedLocationIdHash = "0f37dac11d1b8118ea0b44303400faa5e3b876da9d758058b5ff7dc2e5da8230"

        hashedLocationId.hex() shouldBe expectedLocationIdHash
        hashedLocationId shouldBe expectedLocationIdHash.decodeHex()
    }

    @Test
    fun `test trace location hash generation - 2`() {
        val id: TraceLocationId = "1b02111da7c0799df6ad67deb7b397bdfb07e63da0fdea30fae335762826e34f".decodeHex()
        id.toTraceLocationIdHash().hex() shouldBe "394db434a2e9c2ca9f32eed266d30bc037b4314cb3d53249fada68de45450cbb"
    }

    @Test
    fun `test trace location hash generation - 3`() {
        val id: TraceLocationId = "14c7a20ed81ebabdc32c8521382c56b851af15ccd8d13c86cd91a0620e78d664".decodeHex()
        id.toTraceLocationIdHash().hex() shouldBe "852475fa271a29c67ad85578bb86ff4922dabf9f2b081353e1b5cdf99442889d"
    }

    @Test
    fun `turn location ID into location ID hash`() {
        val traceLocationIdHex = "afa27b44d43b02a9fea41d13cedc2e4016cfcf87c5dbf990e593669aa8ce286d"
        val expectedLocationIdHashHex = "0f37dac11d1b8118ea0b44303400faa5e3b876da9d758058b5ff7dc2e5da8230"

        val traceLocationId: TraceLocationId = traceLocationIdHex.decodeHex()
        traceLocationId.toTraceLocationIdHash() shouldBe expectedLocationIdHashHex.decodeHex()
    }

    @Test
    fun `mock server test data - 1`() {
        val qrCodePayloadBase64 =
            "CAESKAgBEhRBcHBsZSBDb21wdXRlciwgSW5jLhoOMTk1OCBGb2hlIFJvYWQadggBEmA4xNrp5hKJoO_yVbXfF1gS8Yc5nURhOIVLG3nUcSg8IPsI2e8JSIhg-FrHUymQ3RR80KUKb1lZjLQkfTUINUP16r6-jFDURwUlCQQi6NXCgI0rQw0a4MrVrKMbF4NzhQMaENXiVYke5XY0HddkDmj-3HYiBwgBEAQYqQE"
        val expectedLocationIdHex = "fc925439f45417a14403b25c95fdc6d8711653f8aa08c0d0967bd30a6348c7fc"
        val expectedLocationIdBase64 = "/JJUOfRUF6FEA7Jclf3G2HEWU/iqCMDQlnvTCmNIx/w="

        val qrCodePayload = TraceLocationOuterClass.QRCodePayload.parseFrom(
            qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        )

        // Payload -> locationId (direct from raw)
        val payloadBytes = qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        val totalByteSequence = "CWA-GUID".toByteArray() + payloadBytes
        totalByteSequence.toByteString().sha256().base64() shouldBe expectedLocationIdBase64

        // TraceLocation -> locationId (manual reconstruction)
        val traceLocation = TraceLocation(
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_FOOD_SERVICE,
            description = "Apple Computer, Inc.",
            address = "1958 Fohe Road",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = 169,
            cryptographicSeed = "1eJViR7ldjQd12QOaP7cdg==".decodeBase64()!!,
            cnPublicKey = "OMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9eq+voxQ1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UD",
            version = TraceLocation.VERSION
        ).apply {
            locationId.base64() shouldBe expectedLocationIdBase64
            locationId.hex() shouldBe expectedLocationIdHex
        }

        // Full circle: Payload -> tracelocation -> qrpayload -> bytearray -> locationId
        qrCodePayload.traceLocation().apply {
            description shouldBe "Apple Computer, Inc."
            locationId.base64() shouldBe expectedLocationIdBase64
            locationId.hex() shouldBe expectedLocationIdHex
        }

        // ByteArrays actually match, we construct the data the same way.
        qrCodePayload.toByteArray() shouldBe traceLocation.qrCodePayload().toByteArray()
    }

    @Test
    fun `mock server test data - 2`() {
        val qrCodePayloadBase64 =
            "CAESKQgBEhRXYXN0ZSBNYW5hZ2VtZW50IEluYxoPMzc5IE96Zm9jIE1hbm9yGnYIARJgOMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9eq-voxQ1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UDGhCUpoAjtgZJPU4suuQZ6dVUIgcIARAIGJ4E"
        val expectedLocationIdHex = "2c4e7a3be61004ed952cc189e85039e01be65f3d82439c8c7fe0f23b12ffa523"
        val expectedLocationIdBase64 = "LE56O+YQBO2VLMGJ6FA54BvmXz2CQ5yMf+DyOxL/pSM="

        // Payload -> locationId (direct from raw)
        val payloadBytes = qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        val totalByteSequence = "CWA-GUID".toByteArray() + payloadBytes
        totalByteSequence.toByteString().sha256().base64() shouldBe expectedLocationIdBase64

        // TraceLocation -> locationId (manual reconstruction)
        val traceLocation = TraceLocation(
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_PUBLIC_BUILDING,
            description = "Waste Management Inc",
            address = "379 Ozfoc Manor",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = 542,
            cryptographicSeed = "lKaAI7YGST1OLLrkGenVVA==".decodeBase64()!!,
            cnPublicKey = "OMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9eq+voxQ1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UD",
            version = TraceLocation.VERSION
        ).apply {
            locationId.base64() shouldBe expectedLocationIdBase64
            locationId.hex() shouldBe expectedLocationIdHex
        }

        // Full circle: Payload -> tracelocation -> qrpayload -> bytearray -> locationId
        val qrCodePayload = TraceLocationOuterClass.QRCodePayload.parseFrom(
            qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        )

        qrCodePayload.traceLocation().apply {
            description shouldBe "Waste Management Inc"
            locationId.base64() shouldBe expectedLocationIdBase64
            locationId.hex() shouldBe expectedLocationIdHex
        }

        // ByteArrays actually match, we construct the data the same way.
        qrCodePayload.toByteArray() shouldBe traceLocation.qrCodePayload().toByteArray()
    }

    @Test
    fun `mock server test data - 3`() {
        val qrCodePayloadBase64 =
            "CAESMggBEg9NZXRhbHMgVVNBIEluYy4aETk2NiBEaXZ1ZCBIZWlnaHRzKLD6qYMGMNDZtoMGGnYIARJgOMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9eq-voxQ1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UDGhD_mjsnT4b74d8M1P4cUflpIgcIARAKGOkJ"
        val expectedLocationIdHex = "7051e04206ef9caf3a3165e82d0fec4cfe7ade770a2e01d9c0e456add760934d"
        val expectedLocationIdBase64 = "cFHgQgbvnK86MWXoLQ/sTP563ncKLgHZwORWrddgk00="

        // Payload -> locationId (direct from raw)
        val payloadBytes = qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        val totalByteSequence = "CWA-GUID".toByteArray() + payloadBytes
        totalByteSequence.toByteString().sha256().apply {
            base64() shouldBe expectedLocationIdBase64
        }

        // TraceLocation -> locationId (manual reconstruction)
        val traceLocation = TraceLocation(
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
            description = "Metals USA Inc.",
            address = "966 Divud Heights",
            startDate = Instant.ofEpochSecond(1617591600),
            endDate = Instant.ofEpochSecond(1617800400),
            defaultCheckInLengthInMinutes = 1257,
            cryptographicSeed = "/5o7J0+G++HfDNT+HFH5aQ==".decodeBase64()!!,
            cnPublicKey = "OMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9eq+voxQ1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UD",
            version = TraceLocation.VERSION
        ).apply {
            locationId.base64() shouldBe expectedLocationIdBase64
            locationId.hex() shouldBe expectedLocationIdHex
        }

        // Full circle: Payload -> tracelocation -> qrpayload -> bytearray -> locationId
        val qrCodePayload = TraceLocationOuterClass.QRCodePayload.parseFrom(
            qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        )

        qrCodePayload.traceLocation().apply {
            description shouldBe "Metals USA Inc."
            locationId.base64() shouldBe expectedLocationIdBase64
            locationId.hex() shouldBe expectedLocationIdHex
        }

        // ByteArrays actually match, we construct the data the same way.
        qrCodePayload.toByteArray() shouldBe traceLocation.qrCodePayload().toByteArray()
    }
}
