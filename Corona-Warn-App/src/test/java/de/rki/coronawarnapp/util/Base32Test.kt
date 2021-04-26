package de.rki.coronawarnapp.util

import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest

class Base32Test : BaseTest() {

    @ParameterizedTest
    @ArgumentsSource(NoPaddingTestProvider::class)
    fun `Encoding with base32NoPadding`(input: String, expected: String) {
        input.base32(padding = false) shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(WithPaddingTestProvider::class)
    fun `Encoding with base32WithPadding`(input: String, expected: String) {
        input.base32() shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(NoPaddingTestProvider::class)
    fun `Encoding ByteString with base32NoPadding`(input: String, expected: String) {
        val byteString = input.toByteArray().toByteString()
        byteString.base32(padding = false) shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(WithPaddingTestProvider::class)
    fun `Encoding ByteString with base32WithPadding`(input: String, expected: String) {
        val byteString = input.toByteArray().toByteString()
        byteString.base32() shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(WithPaddingTestProvider::class)
    fun `Decoding with base32WithPadding ByteString`(expected: String, input: String) {
        input.decodeBase32().string(Charsets.UTF_8) shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(NoPaddingTestProvider::class)
    fun `Decoding with base32NoPadding ByteString`(expected: String, input: String) {
        input.decodeBase32().string(Charsets.UTF_8) shouldBe expected
    }
}
