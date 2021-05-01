package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class AutoCheckoutHelperTest {

    @ParameterizedTest
    @MethodSource("provideArguments")
    fun `roundToNearest15Minutes() should round correctly`(testCase: TestCase) = with(testCase) {
        roundToNearest15Minutes(minutesToRound) shouldBe expectedRoundingResult
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun provideArguments() = listOf(
            TestCase(
                minutesToRound = 0,
                expectedRoundingResult = 0
            ),
            TestCase(
                minutesToRound = 7,
                expectedRoundingResult = 0
            ),
            TestCase(
                minutesToRound = 8,
                expectedRoundingResult = 15
            ),
            TestCase(
                minutesToRound = 22,
                expectedRoundingResult = 15
            ),
            TestCase(
                minutesToRound = 23,
                expectedRoundingResult = 30
            )
        )
    }

    data class TestCase(
        val minutesToRound: Int,
        val expectedRoundingResult: Int
    )
}
