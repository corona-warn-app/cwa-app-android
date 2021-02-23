package de.rki.coronawarnapp.eventregistration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class Base32EncoderTest {

    private val encoder = Base32Encoder()

    @ParameterizedTest
    @ArgumentsSource(NoPaddingTestProvider::class)
    fun `Test encode with base32NoPadding`(input: String, expected: String) {
        encoder.encode(input, padding = false) shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(WithPaddingTestProvider::class)
    fun `Test encode with base32WithPadding`(input: String, expected: String) {
        encoder.encode(input) shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(WithPaddingTestProvider::class)
    fun `Test decode with base32WithPadding`(expected: String, input: String) {
        encoder.decodeToString(input) shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(NoPaddingTestProvider::class)
    fun `Test decode with base32NoPadding`(expected: String, input: String) {
        encoder.decodeToString(input) shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(WithPaddingTestProvider::class)
    fun `Test decode with base32WithPadding Arrays`(expected: String, input: String) {
        encoder.decode(input) shouldBe expected.toByteArray(Charsets.UTF_8)
    }

    @ParameterizedTest
    @ArgumentsSource(NoPaddingTestProvider::class)
    fun `Test decode with base32NoPadding Arrays`(expected: String, input: String) {
        encoder.decode(input) shouldBe expected.toByteArray(Charsets.UTF_8)
    }
}
