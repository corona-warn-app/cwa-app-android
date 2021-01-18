@file:JvmName("FormatterStatistics")

package de.rki.coronawarnapp.util.formatter

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.contactdiary.util.toFormattedDay
import org.joda.time.LocalDate

fun formatStatisticsDate(localDate: LocalDate, context: Context): String {
    val today = LocalDate()
    val yesterday = today.minusDays(1)
    return when (localDate) {
        today -> {
            context.getString(R.string.statistics_primary_value_today)
        }
        yesterday -> {
            context.getString(R.string.statistics_primary_value_yesterday)
        }
        else -> {
            localDate.toFormattedDay(context.getLocale())
        }
    }
}

fun formatStatisticsDateInterval(localDate: LocalDate, context: Context): String {
    val today = LocalDate()
    val yesterday = today.minusDays(1)
    return when (localDate) {
        today -> {
            context.getString(R.string.statistics_primary_value_until_today)
        }
        yesterday -> {
            context.getString(R.string.statistics_primary_value_until_yesterday)
        }
        else -> {
            localDate.toFormattedDay(context.getLocale())
        }
    }
}


