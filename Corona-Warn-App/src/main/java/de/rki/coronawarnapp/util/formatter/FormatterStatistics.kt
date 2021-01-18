package de.rki.coronawarnapp.util.formatter

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.statistics.IncidenceStats
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.SevenDayRValue
import de.rki.coronawarnapp.statistics.StatsItem
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

fun StatsItem.getPrimaryLabel(context: Context): String {
    val today = LocalDate()
    val yesterday = today.minusDays(1)
    val day = LocalDate(updatedAt)
    val dateTimeFormatter = DateTimeFormat.mediumDate().withLocale(context.getLocale())

    return when (this) {
        is InfectionStats,
        is KeySubmissionsStats -> when (day) {
            today -> context.getString(R.string.statistics_primary_value_today)
            yesterday -> context.getString(R.string.statistics_primary_value_yesterday)
            else -> dateTimeFormatter.print(day)
        }
        is IncidenceStats -> when (day) {
            today -> context.getString(R.string.statistics_primary_value_until_today)
            yesterday -> context.getString(R.string.statistics_primary_value_until_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, dateTimeFormatter.print(day))
        }
        is SevenDayRValue -> when (day) {
            today -> context.getString(R.string.statistics_primary_value_current)
            yesterday -> context.getString(R.string.statistics_primary_value_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, dateTimeFormatter.print(day))
        }
    }
}
