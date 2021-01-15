package de.rki.coronawarnapp.statistics.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import java.util.Locale

internal class StatisticsNumberValueFormatterTest {

    @ParameterizedTest
    @MethodSource("provideArguments")
    fun getFormattedNumberValue(
        value: Double,
        decimals: Int,
        locale: Locale,
        suffixMillion: String,
        expected: String) {

        StatisticsNumberValueFormatter
            .getFormattedNumberValue(value, decimals, locale, suffixMillion) shouldBe expected
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun provideArguments() = listOf(

            Arguments.of(12.3456, -1, Locale.GERMANY,"Mio.", "12"),
            Arguments.of(12.3456, 0, Locale.GERMANY,"Mio.", "12"),
            Arguments.of(12.3456, 1, Locale.GERMANY,"Mio.", "12,3"),
            Arguments.of(12.3456, 2, Locale.GERMANY,"Mio.", "12,35"),

            Arguments.of(12.3456, 1, Locale.UK,"Mio.", "12.3"),

            Arguments.of(12.6543, -1, Locale.GERMANY,"Mio.", "13"),
            Arguments.of(12.6543, 0, Locale.GERMANY,"Mio.", "13"),
            Arguments.of(12.6543, 1, Locale.GERMANY,"Mio.", "12,7"),
            Arguments.of(12.6543, 2, Locale.GERMANY,"Mio.", "12,65"),

            Arguments.of(9_999_999, 0, Locale.GERMANY,"Mio.", "9.999.999"),
            Arguments.of(9_999_999, 0, Locale.UK,"Mio.", "9,999,999"),

            Arguments.of(10_000_000, 0, Locale.GERMANY,"Mio.", "10,0 Mio."),
            Arguments.of(12_345_678, 0, Locale.GERMANY,"Mio.", "12,3 Mio."),
            Arguments.of(12_654_321, 0, Locale.GERMANY,"Mio.", "12,7 Mio."),
            Arguments.of(12_654_321, 0, Locale.UK,"Mio.", "12.7 Mio."),
        )
    }
}
