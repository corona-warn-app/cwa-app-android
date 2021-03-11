package de.rki.coronawarnapp.ui.eventregistration.attendee.checkin

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifyResult
import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.evreg.SignedEventOuterClass
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import testhelpers.BaseTestInstrumentation

@RunWith(JUnit4::class)
class VerifiedTraceLocationKtTest : BaseTestInstrumentation() {

    @Test
    fun testVerifiedTraceLocationMapping() {
        shouldNotThrowAny {
            val signedTraceLocation =
                SignedEventOuterClass.SignedEvent.parseFrom(DECODED_TRACE_LOCATION.decodeBase32().toByteArray())
            val verifiedTraceLocation =
                QRCodeVerifyResult(singedTraceLocation = signedTraceLocation).toVerifiedTraceLocation()
            verifiedTraceLocation shouldBe VerifiedTraceLocation(
                guid = "Yc48RFi/hfyXKlF4DEDs/w==",
                start = Instant.parse("1970-02-01T02:39:15.000Z"),
                end = Instant.parse("1970-02-01T02:39:51.000Z"),
                defaultCheckInLengthInMinutes = 30,
                description = "CWA Launch Party"
            )
        }
    }

    companion object {
        private const val DECODED_TRACE_LOCATION =
            "BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBGGC5LOMNUCAUDBOJ2HSGGTQ6SACIHXQ6SAC" +
                "KA6CJEDARQCEEAPHGEZ5JI2K2T422L5U3SMZY5DGCPUZ2RQACAYEJ3HQYMAFFBU2SQCEEAJAUCJSQJ7WDM6" +
                "75MCMOD3L2UL7ECJU7TYERH23B746RQTABO3CTI="
    }
}
