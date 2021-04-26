package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest

class TraceLocationVerifierTest : BaseTest() {
    fun createInstance() = TraceLocationVerifier()

    @ParameterizedTest
    @ArgumentsSource(ValidQRCodeProvider::class)
    fun `Valid QR Codes`(
        protoQrCodePayload: TraceLocationOuterClass.QRCodePayload
    ) {
        val validationResult = createInstance().verifyTraceLocation(protoQrCodePayload)

        (validationResult is TraceLocationVerifier.VerificationResult.Valid) shouldBe true
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidQRCodeProvider::class)
    fun `Invalid QR Codes`(
        protoQrCodePayload: TraceLocationOuterClass.QRCodePayload,
        expectedFailure: TraceLocationVerifier.VerificationResult.Invalid
    ) {
        val validationResult = createInstance().verifyTraceLocation(protoQrCodePayload)

        validationResult shouldBe expectedFailure
    }
}
