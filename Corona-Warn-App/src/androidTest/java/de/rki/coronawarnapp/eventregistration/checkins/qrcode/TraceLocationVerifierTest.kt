package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import android.os.Bundle
import android.os.Parcel
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.eventregistration.common.base32
import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.security.SignatureValidation
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import testhelpers.BaseTestInstrumentation

@RunWith(JUnit4::class)
class TraceLocationVerifierTest : BaseTestInstrumentation() {

    @MockK lateinit var environmentSetup: EnvironmentSetup
    private lateinit var traceLocationQRCodeVerifier: TraceLocationQRCodeVerifier

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { environmentSetup.appConfigVerificationKey } returns PUB_KEY
        traceLocationQRCodeVerifier = TraceLocationQRCodeVerifier(SignatureValidation(environmentSetup))
    }

    @Test
    fun verifyEventSuccess() = runBlockingTest {
        val instant = Instant.ofEpochMilli(2687960 * 1_000L)
        shouldNotThrowAny {
            val verifyResult = traceLocationQRCodeVerifier.verify(ENCODED_EVENT1.decodeBase32().toByteArray())
            verifyResult.apply {
                traceLocation.description shouldBe "My Birthday Party"
                verifyResult.isBeforeStartTime(instant) shouldBe false
                verifyResult.isAfterEndTime(instant) shouldBe false
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
            signature = verifyResult.signedTraceLocation.signature.toByteArray().toByteString()
        )

        verifyResult.verifiedTraceLocation shouldBe expectedTraceLocation

        val bundle = Bundle().apply {
            putParcelable("test", verifyResult.verifiedTraceLocation)
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
            }
            verifyResult.isBeforeStartTime(instant) shouldBe true
            verifyResult.isAfterEndTime(instant) shouldBe false
        }
    }

    @Test
    fun verifyEventEndTimeWarning() = runBlockingTest {
        val instant = Instant.now()
        shouldNotThrowAny {
            val verifyResult = traceLocationQRCodeVerifier.verify(ENCODED_EVENT1.decodeBase32().toByteArray())
            verifyResult.apply {
                traceLocation.description shouldBe "My Birthday Party"
            }
            verifyResult.isBeforeStartTime(instant) shouldBe false
            verifyResult.isAfterEndTime(instant) shouldBe true
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
            val verifiedTraceLocation = TraceLocationVerifyResult(
                signedTraceLocation = signedTraceLocation,
                traceLocation = traceLocation
            ).verifiedTraceLocation

            verifiedTraceLocation shouldBe TraceLocation(
                guid = "3055331c-2306-43f3-9742-6d8fab54e848",
                version = 1,
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
                description = "My Birthday Party",
                address = "at my place",
                startDate = Instant.ofEpochSecond(2687955),
                endDate = Instant.ofEpochSecond(2687991),
                defaultCheckInLengthInMinutes = 0,
                signature = signedTraceLocation.signature.toByteArray().toByteString()
            )
        }
    }

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
