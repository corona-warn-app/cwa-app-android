package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.environment.EnvironmentSetup
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
import org.joda.time.Instant
import org.junit.Before
import org.junit.Ignore
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
            val verifyResult = qrCodeVerifier.verify(ENCODED_EVENT)
            verifyResult.apply {
                singedTraceLocation.location.description shouldBe "CWA Launch Party"
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
            val verifyResult = qrCodeVerifier.verify(ENCODED_EVENT)
            verifyResult.apply {
                singedTraceLocation.location.description shouldBe "CWA Launch Party"
            }
            verifyResult.isBeforeStartTime(instant) shouldBe true
            verifyResult.isAfterEndTime(instant) shouldBe false
        }
    }

    @Test
    fun verifyEventEndTimeWarning() = runBlockingTest {
        val instant = Instant.now()
        shouldNotThrowAny {
            val verifyResult = qrCodeVerifier.verify(ENCODED_EVENT)
            verifyResult.apply {
                singedTraceLocation.location.description shouldBe "CWA Launch Party"
            }
            verifyResult.isBeforeStartTime(instant) shouldBe false
            verifyResult.isAfterEndTime(instant) shouldBe true
        }
    }

    @Test
    fun verifyEventWithInvalidKey() = runBlockingTest {
        every { environmentSetup.appConfigVerificationKey } returns INVALID_PUB_KEY
        shouldThrow<InvalidQRCodeSignatureException> {
            qrCodeVerifier.verify(ENCODED_EVENT)
        }
    }

    @Test
    fun eventHasMalformedData() = runBlockingTest {
        shouldThrow<InvalidQRCodeDataException> {
            qrCodeVerifier.verify(INVALID_ENCODED_EVENT)
        }
    }

    companion object {

        private const val INVALID_PUB_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg" +
            "3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg=="

        private const val PUB_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEafIKZOiRPuJWjKOUmKv7OTJWTyii" +
                "4oCQLcGn3FgYoLQaJIvAM3Pl7anFDPPY/jxfqqrLyGc0f6hWQ9JPR3QjBw=="

        private const val INVALID_ENCODED_EVENT =
            "BIPEY33SMVWSA2LQON2W2IDEN5WG64RAONUXIIDBNVSXILBAMNXRBCM4UQARRKM6UQASAHRKCC7CTDWGQ" +
                "4JCO7RVZSWVIMQK4UPA.GBCAEIA7TEORBTUA25QHBOCWT26BCA5PORBS2E4FFWMJ3U" +
                "U3P6SXOL7SHUBCA7UEZBDDQ2R6VRJH7WBJKVF7GZYJA6YMRN27IPEP7NKGGJSWX3XQ"

        private const val ENCODED_EVENT =
            "BJLAUJBTGA2TKMZTGFRS2MRTGA3C2NBTMYZS2OJXGQZC2NTEHBTGCYRVGRSTQNBYCAARQARCCFGXSICCNFZHI2DEMF4SAUDBOJ2HSKQL" +
                "MF2CA3LZEBYGYYLDMUYNHB5EAE4PPB5EAFAAAESIGBDAEIIARVENF6QT6XZATJ5GSDHL" +
                "77BCAGR6QKDEUJRP2RDCTKTS7QECWMFAEIIA47MT2EA7MQKGNQU2XCY3Y2ZOZXCILDPC65PBUO4JJHT5LQQWDQSA"
    }
}
