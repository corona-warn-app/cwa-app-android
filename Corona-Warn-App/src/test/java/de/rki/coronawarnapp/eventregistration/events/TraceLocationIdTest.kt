package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TraceLocationIdTest : BaseTest() {
    @Test
    fun `locationId 1`() {
        val qrCodePayloadBase64 =
            "CAESLAgBEhFNeSBCaXJ0aGRheSBQYXJ0eRoLYXQgbXkgcGxhY2Uo04ekATD3h6QBGmUIARJbMFkwEwYHKoZIzj0CAQYIKo" +
                "ZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxe" +
                "uFMZAIX2+6A5XhoEMTIzNCIECAEQAg=="
        val qrCodePayload = TraceLocationOuterClass.QRCodePayload.parseFrom(
            qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        )
        createInstance().locationId(qrCodePayload).toByteString().base64() shouldBe
            "jNcJTCajd9Sen6Tbexl2Yb7O3J7ps47b6k4+QMT4xS0="
    }

    @Test
    fun `locationId 2`() {
        val qrCodePayloadBase64 =
            "CAESIAgBEg1JY2VjcmVhbSBTaG9wGg1NYWluIFN0cmVldCAxGmUIARJbMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIR" +
                "cyk35OYDJ95/hTg3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5XhoEMTIzNCIGCAEQARgK"
        val qrCodePayload = TraceLocationOuterClass.QRCodePayload.parseFrom(
            qrCodePayloadBase64.decodeBase64()!!.toByteArray()
        )
        createInstance().locationId(qrCodePayload).toByteString().base64() shouldBe
            "GMuCjqNmOdYyrFhyvFNTVEeLaZh+uShgUoY0LYJo4YQ="
    }

    private fun createInstance() = TraceLocationId()
}
