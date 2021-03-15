package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifyResult
import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import testhelpers.BaseTestInstrumentation

@RunWith(JUnit4::class)
@Suppress("MaxLineLength")
class VerifiedTraceLocationKtTest : BaseTestInstrumentation() {

    @Test
    fun testVerifiedTraceLocationMapping() {
        shouldNotThrowAny {
            val signedTraceLocation = TraceLocationOuterClass.SignedTraceLocation.parseFrom(
                "BJLAUJBTGA2TKMZTGFRS2MRTGA3C2NBTMYZS2OJXGQZC2NTEHBTGCYRVGRSTQNBYCAARQARCCFGXSICCNFZHI2DEMF4SAUDBOJ2HSKQLMF2CA3LZEBYGYYLDMUYNHB5EAE4PPB5EAFAAAESIGBDAEIIARVENF6QT6XZATJ5GSDHL77BCAGR6QKDEUJRP2RDCTKTS7QECWMFAEIIA47MT2EA7MQKGNQU2XCY3Y2ZOZXCILDPC65PBUO4JJHT5LQQWDQSA"
                    .decodeBase32().toByteArray()
            )

            val traceLocation = TraceLocationOuterClass.TraceLocation.parseFrom(
                "BISDGMBVGUZTGMLDFUZDGMBWFU2DGZRTFU4TONBSFU3GIODGMFRDKNDFHA2DQEABDABCEEKNPEQEE2LSORUGIYLZEBIGC4TUPEVAWYLUEBWXSIDQNRQWGZJQ2OD2IAJY66D2IAKAAA"
                    .decodeBase32().toByteArray()
            )
            val verifiedTraceLocation = QRCodeVerifyResult(
                singedTraceLocation = signedTraceLocation,
                traceLocation = traceLocation
            ).toVerifiedTraceLocation()

            verifiedTraceLocation shouldBe VerifiedTraceLocation(
                guid = "MzA1NTMzMWMtMjMwNi00M2YzLTk3NDItNmQ4ZmFiNTRlODQ4",
                start = Instant.ofEpochSecond(2687955),
                end = Instant.ofEpochSecond(2687991),
                defaultCheckInLengthInMinutes = 0,
                description = "My Birthday Party",
            )
        }
    }
}
