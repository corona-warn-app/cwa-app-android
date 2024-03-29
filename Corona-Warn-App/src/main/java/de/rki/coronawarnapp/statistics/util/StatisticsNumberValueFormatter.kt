package de.rki.coronawarnapp.statistics.util

import android.content.Context
import de.rki.coronawarnapp.contactdiary.util.getLocale
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

fun formatStatisticalValue(
    context: Context,
    value: Double,
    decimals: Int
): String {

    val locale = context.getLocale()

    // return strings like "12.7 Mio" for large values
    if (value >= 10_000_000) {
        return DecimalFormat("##,###,###", DecimalFormatSymbols(locale)).format(value)
    }

    return when (decimals) {
        in Int.MIN_VALUE..0 -> DecimalFormat("#,###", DecimalFormatSymbols(locale))
        1 -> DecimalFormat("#,##0.0", DecimalFormatSymbols(locale))
        2 -> DecimalFormat("#,##0.00", DecimalFormatSymbols(locale))
        else -> DecimalFormat("#,##0.000", DecimalFormatSymbols(locale))
    }.format(value)
}

fun formatPercentageValue(value: Double, locale: Locale): String {
    val percentInstance = getPercentInstance(locale)
    return percentInstance.format(value)
}

private fun getPercentInstance(locale: Locale) =
    NumberFormat.getPercentInstance(locale).apply { minimumFractionDigits = 1 }
