package de.rki.coronawarnapp.statistics.util

import android.content.Context
import android.text.SpannableString
import android.text.style.LocaleSpan
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass

/**
 * returns localized spannable string so that screen readers read out decimal values appropriately
 */
fun getLocalizedSpannableString(context: Context, source: String) = SpannableString(source).apply {
    setSpan(LocaleSpan(context.getLocale()), 0, this.length, 0)
}

fun getContentDescriptionForTrends(
    context: Context,
    trend: KeyFigureCardOuterClass.KeyFigure.Trend
): String {
    return context.getString(
        when (trend) {
            KeyFigureCardOuterClass.KeyFigure.Trend.INCREASING -> R.string.statistics_trend_increasing
            KeyFigureCardOuterClass.KeyFigure.Trend.DECREASING -> R.string.statistics_trend_decreasing
            else -> R.string.statistics_trend_stable
        }
    )
}
