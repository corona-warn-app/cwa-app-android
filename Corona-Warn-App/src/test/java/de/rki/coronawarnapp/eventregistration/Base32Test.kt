package de.rki.coronawarnapp.eventregistration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest

class Base32Test : BaseTest() {

    @ParameterizedTest
    @ArgumentsSource(NoPaddingTestProvider::class)
    fun `Encoding with base32NoPadding`(input: String, expected: String) {
        input.encodeBase32ToString(padding = false) shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(WithPaddingTestProvider::class)
    fun `Encoding with base32WithPadding`(input: String, expected: String) {
        input.encodeBase32ToString() shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(WithPaddingTestProvider::class)
    fun `Decoding with base32WithPadding`(expected: String, input: String) {
        input.decodeBase32ToString() shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(NoPaddingTestProvider::class)
    fun `Decoding with base32NoPadding`(expected: String, input: String) {
        input.decodeBase32ToString() shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(WithPaddingTestProvider::class)
    fun `Decoding with base32WithPadding Arrays`(expected: String, input: String) {
        input.decodeBase32ToArray() shouldBe expected.toByteArray(Charsets.UTF_8)
    }

    @ParameterizedTest
    @ArgumentsSource(NoPaddingTestProvider::class)
    fun `Decoding with base32NoPadding Arrays`(expected: String, input: String) {
        input.decodeBase32ToArray() shouldBe expected.toByteArray(Charsets.UTF_8)
    }

    @ParameterizedTest
    @ArgumentsSource(WithPaddingTestProvider::class)
    fun `Decoding with base32WithPadding ByteString`(expected: String, input: String) {
        input.decodeBase32ToByteString().string(Charsets.UTF_8) shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(NoPaddingTestProvider::class)
    fun `Decoding with base32NoPadding ByteString`(expected: String, input: String) {
        input.decodeBase32ToByteString().string(Charsets.UTF_8) shouldBe expected
    }
}
