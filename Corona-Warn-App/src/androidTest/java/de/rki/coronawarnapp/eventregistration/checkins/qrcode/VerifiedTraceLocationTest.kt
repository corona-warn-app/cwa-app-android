package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import okio.ByteString.Companion.decodeBase64
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import testhelpers.BaseTestInstrumentation

@RunWith(JUnit4::class)
class VerifiedTraceLocationTest : BaseTestInstrumentation() {

    @MockK lateinit var environmentSetup: EnvironmentSetup

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { environmentSetup.appConfigVerificationKey } returns PUB_KEY
    }

    // TODO: Ugly but kinda works
    @Test
    fun verifyTraceLocationIdGenerationHash1() {
        val base64Payload = "CAESLAgBEhFNeSBCaXJ0aGRheSBQYXJ0eRoLYXQgbXkgcGxhY2Uo04ekAT" +
            "D3h6QBGmUIARJbMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEst" +
            "cUIRcyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3" +
            "cAfQmxeuFMZAIX2+6A5XhoEMTIzNCIECAEQAg=="
        val base64LocationID = "jNcJTCajd9Sen6Tbexl2Yb7O3J7ps47b6k4+QMT4xS0="

        val qrCodePayload =
            TraceLocationOuterClass.QRCodePayload.parseFrom(base64Payload.decodeBase64()!!.toByteArray())
        val instance = VerifiedTraceLocation(qrCodePayload)

        instance.traceLocationID.sha256().base64() shouldBe base64LocationID
    }

    @Test
    fun verifyTraceLocationIdGenerationHash2() {
        val base64Payload = "CAESIAgBEg1JY2VjcmVhbSBTaG9wGg1NYWluIFN0cmVldCAxGmUIARJ" +
            "bMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT" +
            "0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5XhoEMTIzNCIGCAEQARgK"
        val base64LocationID = "GMuCjqNmOdYyrFhyvFNTVEeLaZh+uShgUoY0LYJo4YQ="

        val qrCodePayload =
            TraceLocationOuterClass.QRCodePayload.parseFrom(base64Payload.decodeBase64()!!.toByteArray())
        val instance = VerifiedTraceLocation(qrCodePayload)

        instance.traceLocationID.sha256().base64() shouldBe base64LocationID
    }

    /* disabled because of incompatibilities due to latest tech spec changes... needs to be re-written anyway

@Test
fun verifyEventSuccess() = runBlockingTest {
    val instant = Instant.ofEpochMilli(2687960 * 1_000L)
    shouldNotThrowAny {
        val verifyResult = traceLocationQRCodeVerifier.verify(ENCODED_EVENT1.decodeBase32().toByteArray())
        verifyResult.apply {
            traceLocation.description shouldBe "My Birthday Party"
            traceLocation.isBeforeStartTime(instant) shouldBe false
            traceLocation.isAfterEndTime(instant) shouldBe false
        }
    }
}

@Test
fun verifyParcelization() = runBlockingTest {
    val verifyResult = traceLocationQRCodeVerifier.verify(ENCODED_EVENT1.decodeBase32().toByteArray())

    val expectedTraceLocation = TraceLocation(
        guid = "3055331c-2306-43f3-9742-6d8fab54e848",
        version = 1,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
        description = "My Birthday Party",
        address = "at my place",
        startDate = Instant.ofEpochSecond(2687955),
        endDate = Instant.ofEpochSecond(2687991),
        defaultCheckInLengthInMinutes = 0,
        byteRepresentation = verifyResult.traceLocationBytes,
        signature = verifyResult.signature.toByteArray().toByteString(),
    )

    verifyResult.traceLocation shouldBe expectedTraceLocation

    val bundle = Bundle().apply {
        putParcelable("test", verifyResult.traceLocation)
    }

    val parcelRaw = Parcel.obtain().apply {
        writeBundle(bundle)
    }.marshall()

    val restoredParcel = Parcel.obtain().apply {
        unmarshall(parcelRaw, 0, parcelRaw.size)
        setDataPosition(0)
    }

    val restoredData = restoredParcel.readBundle()!!.run {
        classLoader = TraceLocation::class.java.classLoader
        getParcelable<TraceLocation>("test")
    }
    restoredData shouldBe expectedTraceLocation
}

@Test
fun verifyEventStartTimeWaning() = runBlockingTest {
    val instant = Instant.ofEpochMilli(2687940 * 1_000L)
    shouldNotThrowAny {
        val verifyResult = traceLocationQRCodeVerifier.verify(ENCODED_EVENT1.decodeBase32().toByteArray())
        verifyResult.apply {
            traceLocation.description shouldBe "My Birthday Party"
            traceLocation.isBeforeStartTime(instant) shouldBe true
            traceLocation.isAfterEndTime(instant) shouldBe false
        }
    }
}

@Test
fun verifyEventEndTimeWarning() = runBlockingTest {
    val instant = Instant.now()
    shouldNotThrowAny {
        val verifyResult = traceLocationQRCodeVerifier.verify(ENCODED_EVENT1.decodeBase32().toByteArray())
        verifyResult.apply {
            traceLocation.description shouldBe "My Birthday Party"
            traceLocation.isBeforeStartTime(instant) shouldBe false
            traceLocation.isAfterEndTime(instant) shouldBe true
        }
    }
}

@Test
fun verifyEventWithInvalidKey() = runBlockingTest {
    every { environmentSetup.appConfigVerificationKey } returns INVALID_PUB_KEY
    shouldThrow<InvalidQRCodeSignatureException> {
        traceLocationQRCodeVerifier.verify(ENCODED_EVENT1.decodeBase32().toByteArray())
    }
}

@Test
fun eventHasMalformedData() = runBlockingTest {
    shouldThrow<InvalidQRCodeDataException> {
        traceLocationQRCodeVerifier.verify(
            INVALID_ENCODED_EVENT.decodeBase32().toByteArray()
        )
    }
}

@Test
fun decodingTest1() = runBlockingTest {
    val signedTraceLocation = TraceLocationOuterClass.SignedTraceLocation.parseFrom(
        ENCODED_EVENT1.decodeBase32().toByteArray()
    )
    val expectedSignature =
        "MEQCIGVKfqPF2851IrEyDeVMazlRnIzLX16H6r1TB37PRzjbAiBGP13ADQcbQZsztKUCZMRcvnv5Mgdo0LY/v3qFMnrUkQ=="

    val base32 = signedTraceLocation.toByteArray().toByteString().base32()

    shouldNotThrowAny {
        val verifyResult = traceLocationQRCodeVerifier.verify(base32.decodeBase32().toByteArray())

        verifyResult.apply {
            traceLocation.description shouldBe "My Birthday Party"
            signedTraceLocation.signature.toByteArray().toByteString().base64() shouldBe expectedSignature
        }
    }
}

@Test
fun decodingTest2() = runBlockingTest {
    val signedTraceLocation = TraceLocationOuterClass.SignedTraceLocation.parseFrom(
        ENCODED_EVENT2.decodeBase32().toByteArray()
    )
    val expectedSignature =
        "MEQCIDWRTM4ujn1GFPuHlgpUnQIWwwzwI8abxSrF5Er2I5HaAiAbucxg+6d3nC/Iwzo7AXehJAS20TRX1S2rl0LO8kcYxA=="

    val base32 = signedTraceLocation.toByteArray().toByteString().base32()

    shouldNotThrowAny {
        val verifyResult = traceLocationQRCodeVerifier.verify(base32.decodeBase32().toByteArray())

        verifyResult.apply {
            traceLocation.description shouldBe "Icecream Shop"
            signedTraceLocation.signature.toByteArray().toByteString().base64() shouldBe expectedSignature
        }
    }
}

@Test
fun testVerifiedTraceLocationMapping() {
    shouldNotThrowAny {
        val signedTraceLocation = TraceLocationOuterClass.SignedTraceLocation.parseFrom(
            ENCODED_EVENT1.decodeBase32().toByteArray()
        )

        val traceLocation = TraceLocationOuterClass.TraceLocation.parseFrom(
            ENCODED_EVENT1_LOCATION.decodeBase32().toByteArray()
        )
        val verifiedTraceLocation = VerifiedTraceLocation(
            protoSignedTraceLocation = signedTraceLocation,
            protoTraceLocation = traceLocation
        ).traceLocation

        verifiedTraceLocation shouldBe TraceLocation(
            guid = "3055331c-2306-43f3-9742-6d8fab54e848",
            version = 1,
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
            description = "My Birthday Party",
            address = "at my place",
            startDate = Instant.ofEpochSecond(2687955),
            endDate = Instant.ofEpochSecond(2687991),
            defaultCheckInLengthInMinutes = 0,
            byteRepresentation = signedTraceLocation.location.toByteArray().toByteString(),
            signature = signedTraceLocation.signature.toByteArray().toByteString()
        )
    }
}

*/

    companion object {

        //   "signedLocation": {
        //    "location": {
        //      "guid": "3055331c-2306-43f3-9742-6d8fab54e848",
        //      "version": 1,
        //      "type": 2,
        //      "description": "My Birthday Party",
        //      "address": "at my place",
        //      "startTimestamp": 2687955,
        //      "endTimestamp": 2687991,
        //      "defaultCheckInLengthInMinutes": 0
        //    },
        //    "signature": "MEQCIGVKfqPF2851IrEyDeVMazlRnIzLX16H6r1TB37PRzjbAiBGP13ADQcbQZsztKUCZMRcvnv5Mgdo0LY/v3qFMnrUkQ=="
        private const val ENCODED_EVENT1 =
            "BJLAUJBTGA2TKMZTGFRS2MRTGA3C2NBTMYZS2OJXGQZC2NTEHBTGCYRVGRSTQNBYCAARQARCCFGXSICCNFZHI2DEMF4SAUDBOJ2HSKQLMF2CA3LZEBYGYYLDMUYNHB5EAE4PPB5EAFAAAESGGBCAEIDFJJ7KHRO3ZZ2SFMJSBXSUY2ZZKGOIZS27L2D6VPKTA57M6RZY3MBCARR7LXAA2BY3IGNTHNFFAJSMIXF6PP4TEB3I2C3D7P32QUZHVVER"
        private const val ENCODED_EVENT1_LOCATION =
            "BISDGMBVGUZTGMLDFUZDGMBWFU2DGZRTFU4TONBSFU3GIODGMFRDKNDFHA2DQEABDABCEEKNPEQEE2LSORUGIYLZEBIGC4TUPEVAWYLUEBWXSIDQNRQWGZJQ2OD2IAJY66D2IAKAAA"

        //   "signedLocation": {
        //    "location": {
        //      "guid": "fca84b37-61c0-4a7c-b2f8-825cadd506cf",
        //      "version": 1,
        //      "type": 1,
        //      "description": "Icecream Shop",
        //      "address": "Main Street 1",
        //      "startTimestamp": 0,
        //      "endTimestamp": 0,
        //      "defaultCheckInLengthInMinutes": 10
        //    },
        //    "signature": "MEQCIDWRTM4ujn1GFPuHlgpUnQIWwwzwI8abxSrF5Er2I5HaAiAbucxg+6d3nC/Iwzo7AXehJAS20TRX1S2rl0LO8kcYxA=="
        private const val ENCODED_EVENT2 =
            "BJHAUJDGMNQTQNDCGM3S2NRRMMYC2NDBG5RS2YRSMY4C2OBSGVRWCZDEGUYDMY3GCAARQAJCBVEWGZLDOJSWC3JAKNUG64BKBVGWC2LOEBJXI4TFMV2CAMJQAA4AAQAKCJDDARACEA2ZCTGOF2HH2RQU7ODZMCSUTUBBNQYM6AR4NG6FFLC6ISXWEOI5UARADO44YYH3U53ZYL6IYM5DWALXUESAJNWRGRL5KLNLS5BM54SHDDCA"

        private const val INVALID_ENCODED_EVENT =
            "NB2HI4DTHIXS653XO4XHK4TCMFXGI2LDORUW63TBOJ4S4Y3PNUXWIZLGNFXGKLTQNBYD65DFOJWT2VDIMUSTEMCDN53GSZBFGIYDCOI="

        private const val PUB_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEafIKZOiRPuJWjKOUmKv7OTJWTyii4oCQLcGn3FgYoLQaJIvAM3Pl7anFDPPY/jxfqqrLyGc0f6hWQ9JPR3QjBw=="
        private const val INVALID_PUB_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg=="
    }
}
