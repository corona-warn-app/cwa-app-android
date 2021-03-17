package de.rki.coronawarnapp.ui.durationpicker

import io.kotest.matchers.shouldBe
import org.joda.time.Duration
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DurationExtensionKtTest {

    @ParameterizedTest
    @MethodSource("provideArgumentsForContactDiaryFormat")
    fun `toContactDiaryFormat() should return correct String`(testItem: TestItem) {
        with(testItem) {
            duration.toContactDiaryFormat() shouldBe expectedReadableDuration
        }
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForReadableDuration")
    fun `toReadableDuration() should return correct String`(testItem: TestItem) {
        with(testItem) {
            duration.toReadableDuration(prefix, suffix) shouldBe expectedReadableDuration
        }
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun provideArgumentsForContactDiaryFormat() = listOf(
            TestItem(
                duration = Duration.standardMinutes(0),
                expectedReadableDuration = "00:00"
            ),
            TestItem(
                duration = Duration.standardMinutes(1),
                expectedReadableDuration = "00:01"
            ),
            TestItem(
                duration = Duration.standardMinutes(30),
                expectedReadableDuration = "00:30"
            ),
            TestItem(
                duration = Duration.standardMinutes(45),
                expectedReadableDuration = "00:45"
            ),
            TestItem(
                duration = Duration.standardMinutes(60),
                expectedReadableDuration = "01:00"
            ),
            TestItem(
                duration = Duration.standardMinutes(75),
                expectedReadableDuration = "01:15"
            ),
        ).map { Arguments.of(it) }

        @Suppress("unused")
        @JvmStatic
        fun provideArgumentsForReadableDuration() = listOf(
            TestItem(
                duration = Duration.standardMinutes(30),
                expectedReadableDuration = "00:30"
            ),
            TestItem(
                prefix = "Dauer",
                duration = Duration.standardMinutes(45),
                expectedReadableDuration = "Dauer 00:45"
            ),
            TestItem(
                suffix = "Std.",
                duration = Duration.standardMinutes(60),
                expectedReadableDuration = "01:00 Std."
            ),
            TestItem(
                prefix = "Dauer",
                suffix = "h",
                duration = Duration.standardMinutes(75),
                expectedReadableDuration = "Dauer 01:15 h"
            ),
        ).map { Arguments.of(it) }
    }

    data class TestItem(
        val prefix: String? = null,
        val duration: Duration,
        val suffix: String? = null,
        val expectedReadableDuration: String
    )
}
