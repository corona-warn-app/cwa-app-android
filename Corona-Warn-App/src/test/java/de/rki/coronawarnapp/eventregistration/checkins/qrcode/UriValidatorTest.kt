package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class UriValidatorTest {

    @ParameterizedTest
    @ArgumentsSource(ValidUrlProvider::class)
    fun `Valid URLs`(input: String) {
        input.isValidQRCodeUri() shouldBe true
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidUrlProvider::class)
    fun `Invalid URLs`(input: String) {
        input.isValidQRCodeUri() shouldBe false
    }

    @Test
    fun `Invalid  URL string`() {
        shouldThrow<IllegalArgumentException> {
            "Hello World!".isValidQRCodeUri()
        }
    }
}
