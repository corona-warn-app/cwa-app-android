package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.eventregistration.events.locationId
import de.rki.coronawarnapp.eventregistration.events.qrCodePayload
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import testhelpers.BaseTestInstrumentation

@RunWith(JUnit4::class)
class VerifiedTraceLocationTest : BaseTestInstrumentation() {
    @Test
    fun verifyTraceLocationMapping1() {
        val base64Payload = "CAESLAgBEhFNeSBCaXJ0aGRheSBQYXJ0eRoLYXQgbXkgcGxhY2Uo04ekAT" +
            "D3h6QBGmUIARJbMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEst" +
            "cUIRcyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3" +
            "cAfQmxeuFMZAIX2+6A5XhoEMTIzNCIECAEQAg=="

        val qrCodePayload = TraceLocationOuterClass.QRCodePayload.parseFrom(
            base64Payload.decodeBase64()!!.toByteArray()
        )

        VerifiedTraceLocation(qrCodePayload).traceLocation.apply {
            locationId.base64() shouldBe "jNcJTCajd9Sen6Tbexl2Yb7O3J7ps47b6k4+QMT4xS0="
            qrCodePayload() shouldBe qrCodePayload
        }
    }

    @Test
    fun verifyTraceLocationMapping2() {
        val base64Payload = "CAESIAgBEg1JY2VjcmVhbSBTaG9wGg1NYWluIFN0cmVldCAxGmUIARJ" +
            "bMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT" +
            "0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5XhoEMTIzNCIGCAEQARgK"

        val qrCodePayload = TraceLocationOuterClass.QRCodePayload.parseFrom(
            base64Payload.decodeBase64()!!.toByteArray()
        )

        VerifiedTraceLocation(qrCodePayload).traceLocation.apply {
            locationId.base64() shouldBe "GMuCjqNmOdYyrFhyvFNTVEeLaZh+uShgUoY0LYJo4YQ="
            qrCodePayload() shouldBe qrCodePayload
        }
    }
}
