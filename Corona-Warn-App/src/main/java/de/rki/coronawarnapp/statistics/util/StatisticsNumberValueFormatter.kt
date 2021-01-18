package de.rki.coronawarnapp.statistics.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object StatisticsNumberValueFormatter {

    fun getFormattedNumberValue(
        value: Double,
        decimals: Int,
        locale: Locale,
        suffixMillion: String
    ): String {

        // return strings like "12.7 Mio" for large values
        if (value >= 10_000_000) {
            return DecimalFormat("#,###.0", DecimalFormatSymbols(locale))
                .format(value / 1_000_000) + " $suffixMillion"
        }

        return when (decimals) {
            in Int.MIN_VALUE..0 -> DecimalFormat("#,###", DecimalFormatSymbols(locale))
            1 -> DecimalFormat("#,###.#", DecimalFormatSymbols(locale))
            else -> DecimalFormat("#,###.##", DecimalFormatSymbols(locale))
        }.format(value)
    }
}
