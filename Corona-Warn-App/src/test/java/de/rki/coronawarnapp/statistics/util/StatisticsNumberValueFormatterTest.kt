package de.rki.coronawarnapp.statistics.util

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class StatisticsNumberValueFormatterTest {

    @MockK
    private lateinit var context: Context

    @BeforeAll
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("de.rki.coronawarnapp.contactdiary.util.ContactDiaryExtensionsKt")
        every { context.getString(R.string.statistics_value_suffix_million) } returns "Mio."
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    fun `getFormattedNumberValue() should return correctly formatted number`(
        value: Double,
        decimals: Int,
        locale: Locale,
        expected: String
    ) {

        every { context.getLocale() } returns locale

        formatStatisticalValue(context, value, decimals) shouldBe expected
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun provideArguments() = listOf(

            Arguments.of(12.3456, -1, Locale.GERMANY, "12"),
            Arguments.of(12.3456, 0, Locale.GERMANY, "12"),
            Arguments.of(12.3456, 1, Locale.GERMANY, "12,3"),
            Arguments.of(12.3456, 2, Locale.GERMANY, "12,35"),

            Arguments.of(1.0036, 1, Locale.GERMANY, "1,0"),
            Arguments.of(1.0036, 2, Locale.GERMANY, "1,00"),
            Arguments.of(1.0, 3, Locale.GERMANY, "1,000"),

            Arguments.of(0.94, 1, Locale.GERMANY, "0,9"),
            Arguments.of(0.95, 2, Locale.GERMANY, "0,95"),

            Arguments.of(12.3456, 1, Locale.UK, "12.3"),

            Arguments.of(12.6543, -1, Locale.GERMANY, "13"),
            Arguments.of(12.6543, 0, Locale.GERMANY, "13"),
            Arguments.of(12.6543, 1, Locale.GERMANY, "12,7"),
            Arguments.of(12.6543, 2, Locale.GERMANY, "12,65"),

            Arguments.of(9_999_999, 0, Locale.GERMANY, "9.999.999"),
            Arguments.of(9_999_999, 0, Locale.UK, "9,999,999"),

            Arguments.of(10_000_000, 0, Locale.GERMANY, "10,0 Mio."),
            Arguments.of(12_345_678, 0, Locale.GERMANY, "12,3 Mio."),
            Arguments.of(12_654_321, 0, Locale.GERMANY, "12,7 Mio."),
            Arguments.of(12_654_321, 0, Locale.UK, "12.7 Mio.")
        )
    }

    @AfterAll
    fun cleanUp() {
        unmockkAll()
    }
}
