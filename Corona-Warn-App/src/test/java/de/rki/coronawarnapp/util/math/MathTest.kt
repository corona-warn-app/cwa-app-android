package de.rki.coronawarnapp.util.math

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

@Suppress("FloatingPointLiteralPrecision")
internal class MathTest : BaseTest() {

    @Test
    fun roundToDecimal() {
        3.5999999999999996.roundToDecimal() shouldBe 3.6
        (1.0 * 1.2 * 3.0).roundToDecimal() shouldBe 3.6

        8.9999999999999996.roundToDecimal() shouldBe 9.0
        (0.6 + 4.8 + 3.5999999999999996).roundToDecimal() shouldBe 9.0
    }
}
