package de.rki.coronawarnapp.ui.eventregistration.attendee.checkin

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifyResult
import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.Ignore
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
                TraceLocationOuterClass.SignedTraceLocation.parseFrom(
                    DECODED_TRACE_LOCATION.decodeBase32().toByteArray()
                )
            val verifiedTraceLocation =
                QRCodeVerifyResult(singedTraceLocation = signedTraceLocation).toVerifiedTraceLocation()
            verifiedTraceLocation shouldBe VerifiedTraceLocation(
                guid = "3055331c-2306-43f3-9742-6d8fab54e848",
                start = Instant.parse("1970-02-01T02:39:15.000Z"),
                end = Instant.parse("1970-02-01T02:39:51.000Z"),
                defaultCheckInLengthInMinutes = 0,
                description = "My Birthday Party"
            )
        }
    }

    companion object {
        private const val DECODED_TRACE_LOCATION =
            "BJLAUJBTGA2TKMZTGFRS2MRTGA3C2NBTMYZS2OJXGQZC2NTEHBTGCYRVGRST" +
                "QNBYCAARQARCCFGXSICCNFZHI2DEMF4SAUDBOJ2HSKQLMF" +
                "2CA3LZEBYGYYLDMUYNHB5EAE4PPB5EAFAAAESIGBDAEIIARVENF6QT6XZATJ5GSDHL77" +
                "BCAGR6QKDEUJRP2RDCTKTS7QECWMFAEIIA47MT2EA7MQKGNQU2XCY3Y2ZOZXCILDPC65PBUO4JJHT5LQQWDQSA"
    }
}
