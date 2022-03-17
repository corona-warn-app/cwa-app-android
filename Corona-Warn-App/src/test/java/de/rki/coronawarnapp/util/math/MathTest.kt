package de.rki.coronawarnapp.util.math

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

@Suppress("FloatingPointLiteralPrecision")
internal class MathTest : BaseTest() {

    @Test
    fun roundToDecimal_1() {
        3.5999999999999996.roundToDecimal(decimalPlacesNumber = 1u) shouldBe 3.6
        (1.0 * 1.2 * 3.0).roundToDecimal(decimalPlacesNumber = 1u) shouldBe 3.6

        8.9999999999999996.roundToDecimal(decimalPlacesNumber = 1u) shouldBe 9.0
        (0.6 + 4.8 + 3.5999999999999996).roundToDecimal(decimalPlacesNumber = 1u) shouldBe 9.0
    }

    @Test
    fun roundToDecimal_2() {
        3.5999999999999996.roundToDecimal(decimalPlacesNumber = 2u) shouldBe 3.6
        (1.0 * 1.2 * 3.0).roundToDecimal(decimalPlacesNumber = 2u) shouldBe 3.6

        8.9999999999999996.roundToDecimal(decimalPlacesNumber = 2u) shouldBe 9.0
        (0.6 + 4.8 + 3.5999999999999996).roundToDecimal(decimalPlacesNumber = 1u) shouldBe 9.0
    }

    @Test
    fun roundToDecimal_3() {
        3.5999999999999996.roundToDecimal(decimalPlacesNumber = 3u) shouldBe 3.6
        (1.0 * 1.2 * 3.0).roundToDecimal(decimalPlacesNumber = 3u) shouldBe 3.6

        8.9999999999999996.roundToDecimal(decimalPlacesNumber = 3u) shouldBe 9.0
        (0.6 + 4.8 + 3.5999999999999996).roundToDecimal(decimalPlacesNumber = 1u) shouldBe 9.0
    }
}
