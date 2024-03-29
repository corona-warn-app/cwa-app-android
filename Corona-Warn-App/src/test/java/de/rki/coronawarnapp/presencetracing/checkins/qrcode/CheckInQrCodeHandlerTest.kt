package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import de.rki.coronawarnapp.qrcode.handler.CheckInQrCodeHandler
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest

class CheckInQrCodeHandlerTest : BaseTest() {
    fun createInstance() = CheckInQrCodeHandler()

    @ParameterizedTest
    @ArgumentsSource(ValidQRCodeProvider::class)
    fun `Valid QR Codes`(
        protoQrCodePayload: TraceLocationOuterClass.QRCodePayload
    ) {
        val validationResult = createInstance().handleQrCode(CheckInQrCode(protoQrCodePayload))

        (validationResult is CheckInQrCodeHandler.Result.Valid) shouldBe true
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidQRCodeProvider::class)
    fun `Invalid QR Codes`(
        protoQrCodePayload: TraceLocationOuterClass.QRCodePayload,
        expectedFailure: CheckInQrCodeHandler.Result.Invalid
    ) {
        val validationResult = createInstance().handleQrCode(CheckInQrCode(protoQrCodePayload))

        validationResult shouldBe expectedFailure
    }
}
