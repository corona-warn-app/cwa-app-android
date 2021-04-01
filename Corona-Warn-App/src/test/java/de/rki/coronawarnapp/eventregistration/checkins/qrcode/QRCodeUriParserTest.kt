package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest

class QRCodeUriParserTest : BaseTest() {

    fun createInstance() = QRCodeUriParser()

    @ParameterizedTest
    @ArgumentsSource(ValidUrlProvider::class)
    fun `Valid URLs`(input: String) {
        createInstance().getQrCodePayload(input) shouldNotBe null
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidUrlProvider::class)
    fun `Invalid URLs`(input: String) {
        createInstance().getQrCodePayload(input) shouldBe null
    }
}
