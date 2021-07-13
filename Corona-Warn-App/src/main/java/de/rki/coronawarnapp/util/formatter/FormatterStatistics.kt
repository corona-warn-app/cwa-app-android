package de.rki.coronawarnapp.util.formatter

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.statistics.AppliedVaccinationRatesStats
import de.rki.coronawarnapp.statistics.GlobalStatsItem
import de.rki.coronawarnapp.statistics.IncidenceStats
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.LocalIncidenceStats
import de.rki.coronawarnapp.statistics.LocalStatsItem
import de.rki.coronawarnapp.statistics.PersonsVaccinatedCompletelyStats
import de.rki.coronawarnapp.statistics.PersonsVaccinatedOnceStats
import de.rki.coronawarnapp.statistics.SevenDayRValue
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

fun GlobalStatsItem.getPrimaryLabel(context: Context): String {
    val today = LocalDate()
    val yesterday = today.minusDays(1)
    val updatedAtDate = LocalDate(updatedAt.toUserTimeZone())
    val dateTimeFormatter = DateTimeFormat.mediumDate().withLocale(context.getLocale())

    return when (this) {
        is InfectionStats,
        is KeySubmissionsStats,
        is AppliedVaccinationRatesStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_today)
            yesterday -> context.getString(R.string.statistics_primary_value_yesterday)
            else -> dateTimeFormatter.print(updatedAtDate)
        }
        is IncidenceStats,
        is PersonsVaccinatedOnceStats,
        is PersonsVaccinatedCompletelyStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_until_today)
            yesterday -> context.getString(R.string.statistics_primary_value_until_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, dateTimeFormatter.print(updatedAtDate))
        }
        is SevenDayRValue -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_current)
            yesterday -> context.getString(R.string.statistics_primary_value_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, dateTimeFormatter.print(updatedAtDate))
        }
    }
}

fun LocalStatsItem.getPrimaryLabel(context: Context): String {
    val today = LocalDate()
    val yesterday = today.minusDays(1)
    val updatedAtDate = LocalDate(updatedAt.toUserTimeZone())
    val dateTimeFormatter = DateTimeFormat.mediumDate().withLocale(context.getLocale())

    return when (this) {
        is LocalIncidenceStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_until_today)
            yesterday -> context.getString(R.string.statistics_primary_value_until_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, dateTimeFormatter.print(updatedAtDate))
        }
    }
}
