package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.util.security.VerificationKeys
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import testhelpers.BaseTestInstrumentation
import testhelpers.TestDispatcherProvider

@RunWith(JUnit4::class)
class DefaultQRCodeVerifierTest : BaseTestInstrumentation() {

    @MockK lateinit var environmentSetup: EnvironmentSetup

    private lateinit var qrCodeVerifier: QRCodeVerifier

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { environmentSetup.appConfigVerificationKey } returns PUB_KEY

        qrCodeVerifier = DefaultQRCodeVerifier(
            TestDispatcherProvider(),
            VerificationKeys(environmentSetup)
        )
    }

    @Test
    fun verify() = runBlockingTest {
        val encodedEvent =
            "BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBGGC5LOMNUCAUDBOJ2" +
                "HSGGTQ6SACIHXQ6SACKA6CJEDARQCEEAPHGEZ5JI2K2T422L5U3SMZY5DGC" +
                "PUZ2RQACAYEJ3HQYMAFFBU2SQCEEAJAUCJSQJ7WDM675MCMOD3L2UL7ECJU" +
                "7TYERH23B746RQTABO3CTI="
        qrCodeVerifier.verify(encodedEvent) shouldBe true
    }

    companion object {
        private const val PUB_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg" +
                "3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg=="
    }
}
