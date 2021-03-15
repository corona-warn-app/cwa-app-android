package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.eventregistration.common.base32
import de.rki.coronawarnapp.eventregistration.common.decodeBase32
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
class DefaultQRCodeVerifierTest : BaseTestInstrumentation() {

    @MockK lateinit var environmentSetup: EnvironmentSetup
    private lateinit var qrCodeVerifier: QRCodeVerifier

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { environmentSetup.appConfigVerificationKey } returns PUB_KEY
        qrCodeVerifier = DefaultQRCodeVerifier(SignatureValidation(environmentSetup))
    }

    @Test
    fun verifyEventSuccess() = runBlockingTest {
        val time = 2687960 * 1_000L
        val instant = Instant.ofEpochMilli(time)
        shouldNotThrowAny {
            val verifyResult = qrCodeVerifier.verify(ENCODED_EVENT.decodeBase32().toByteArray())
            verifyResult.apply {
                traceLocation.description shouldBe "CWA Launch Party"
                verifyResult.isBeforeStartTime(instant) shouldBe false
                verifyResult.isAfterEndTime(instant) shouldBe false
            }
        }
    }

    @Test
    fun verifyEventStartTimeWaning() = runBlockingTest {
        val time = 2687940 * 1_000L
        val instant = Instant.ofEpochMilli(time)
        shouldNotThrowAny {
            val verifyResult = qrCodeVerifier.verify(ENCODED_EVENT.decodeBase32().toByteArray())
            verifyResult.apply {
                traceLocation.description shouldBe "CWA Launch Party"
            }
            verifyResult.isBeforeStartTime(instant) shouldBe true
            verifyResult.isAfterEndTime(instant) shouldBe false
        }
    }

    @Test
    fun verifyEventEndTimeWarning() = runBlockingTest {
        val instant = Instant.now()
        shouldNotThrowAny {
            val verifyResult = qrCodeVerifier.verify(ENCODED_EVENT.decodeBase32().toByteArray())
            verifyResult.apply {
                traceLocation.description shouldBe "CWA Launch Party"
            }
            verifyResult.isBeforeStartTime(instant) shouldBe false
            verifyResult.isAfterEndTime(instant) shouldBe true
        }
    }

    @Test
    fun verifyEventWithInvalidKey() = runBlockingTest {
        every { environmentSetup.appConfigVerificationKey } returns INVALID_PUB_KEY
        shouldThrow<InvalidQRCodeSignatureException> {
            qrCodeVerifier.verify(ENCODED_EVENT.decodeBase32().toByteArray())
        }
    }

    @Test
    fun eventHasMalformedData() = runBlockingTest {
        shouldThrow<InvalidQRCodeDataException> {
            qrCodeVerifier.verify(INVALID_ENCODED_EVENT.decodeBase32().toByteArray())
        }
    }

    @Test
    fun decodingTest1() = runBlockingTest {
        val signedTraceLocation = TraceLocationOuterClass.SignedTraceLocation.newBuilder().apply {
            signature = ByteString.copyFromUtf8(
                "MEYCIQCNSNL6E/XyCaemkM6//CIBo+goZKJi/URimqcvwIKzCgIhAOfZPRAfZBRmwpq4sbxrLs3EhY3i914aO4lJ59XCFhwk"
            )
            location = ByteString.copyFrom(
                "BISDGMBVGUZTGMLDFUZDGMBWFU2DGZRTFU4TONBSFU3GIODGMFRDKNDFHA2DQEABDABCEEKNPEQEE2LSORUGIYLZEBIGC4TUPEVAWYLUEBWXSIDQNRQWGZJQ2OD2IAJY66D2IAKAAA"
                    .decodeBase32().toByteArray()
            )
        }.build()

        val base32 = signedTraceLocation.toByteArray().toByteString().base32()

        shouldNotThrowAny {
            val verifyResult = qrCodeVerifier.verify(base32.decodeBase32().toByteArray())

            verifyResult.apply {
                traceLocation.description shouldBe "My Birthday Party"
                signedTraceLocation.signature shouldBe "MEYCIQCNSNL6E/XyCaemkM6//CIBo+goZKJi/URimqcvwIKzCgIhAOfZPRAfZBRmwpq4sbxrLs3EhY3i914aO4lJ59XCFhwk"
            }
        }
    }

    @Test
    fun decodingTest2() = runBlockingTest {
        val signedTraceLocation = TraceLocationOuterClass.SignedTraceLocation.newBuilder().apply {
            signature = ByteString.copyFromUtf8(
                "MEUCIFpHvUqYAIP0Mq86R7kNO4EgRSvGJHbOlDraauKZvkgbAiEAh93bBDYviEtym4q5Oqzd7j6Dp1MLCP7YwCKlVcU2DHc="
            )
            location = ByteString.copyFrom(
                "BISGMY3BHA2GEMZXFU3DCYZQFU2GCN3DFVRDEZRYFU4DENLDMFSGINJQGZRWMEABDAASEDKJMNSWG4TFMFWSAU3IN5YCUDKNMFUW4ICTORZGKZLUEAYTAABYABAAU"
                    .decodeBase32().toByteArray()
            )
        }.build()

        val base32 = signedTraceLocation.toByteArray().toByteString().base32()

        shouldNotThrowAny {
            val verifyResult = qrCodeVerifier.verify(base32.decodeBase32().toByteArray())

            verifyResult.apply {
                traceLocation.description shouldBe "Icecream Shop"
                signedTraceLocation.signature shouldBe "MEUCIFpHvUqYAIP0Mq86R7kNO4EgRSvGJHbOlDraauKZvkgbAiEAh93bBDYviEtym4q5Oqzd7j6Dp1MLCP7YwCKlVcU2DHc="
            }
        }
    }

    companion object {

        private const val INVALID_PUB_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg" +
            "3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg=="

        private const val PUB_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEafIKZOiRPuJWjKOUmKv7OTJWTyii" +
            "4oCQLcGn3FgYoLQaJIvAM3Pl7anFDPPY/jxfqqrLyGc0f6hWQ9JPR3QjBw=="

        private const val INVALID_ENCODED_EVENT =
            "BIPEY33SMVWSA2LQON2W2IDEN5WG64RAONUXIIDBNVSXILBAMNXRBCM4UQARRKM6UQASAHRKCC7CTDWGQ" +
                "4JCO7RVZSWVIMQK4UPA.GBCAEIA7TEORBTUA25QHBOCWT26BCA5PORBS2E4FFWMJ3U" +
                "U3P6SXOL7SHUBCA7UEZBDDQ2R6VRJH7WBJKVF7GZYJA6YMRN27IPEP7NKGGJSWX3XQ"

        private const val ENCODED_EVENT =
            "BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBGGC5LOMNUCAUDBOJ2" +
                "HSGGTQ6SACIHXQ6SACKA6CJEDARQCEEAPHGEZ5JI2K2T422L5U3SMZY5DGC" +
                "PUZ2RQACAYEJ3HQYMAFFBU2SQCEEAJAUCJSQJ7WDM675MCMOD3L2UL7ECJU" +
                "7TYERH23B746RQTABO3CTI="
    }
}
