package de.rki.coronawarnapp.contactdiary.ui.durationpicker

import io.kotest.matchers.shouldBe
import org.joda.time.Duration
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DurationExtensionKtTest {

    @ParameterizedTest
    @MethodSource("provideArguments")
    fun `toReadableDuration() should return correct String`(testItem: TestItem) {
        with(testItem) {
            duration.toReadableDuration(prefix, suffix) shouldBe expectedReadableDuration
        }
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun provideArguments() = listOf(
            TestItem(
                prefix = null,
                suffix = null,
                duration = Duration.standardMinutes(30),
                expectedReadableDuration = "00:30"
            ),
            TestItem(
                prefix = "Dauer",
                suffix = null,
                duration = Duration.standardMinutes(45),
                expectedReadableDuration = "Dauer 00:45"
            ),
            TestItem(
                prefix = null,
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
        val prefix: String?,
        val duration: Duration,
        val suffix: String?,
        val expectedReadableDuration: String
    )
}
