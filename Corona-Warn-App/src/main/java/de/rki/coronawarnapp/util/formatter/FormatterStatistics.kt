package de.rki.coronawarnapp.util.formatter

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.statistics.AppliedVaccinationRatesStats
import de.rki.coronawarnapp.statistics.IncidenceStats
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.LocalIncidenceStats
import de.rki.coronawarnapp.statistics.PersonsVaccinatedCompletelyStats
import de.rki.coronawarnapp.statistics.PersonsVaccinatedOnceStats
import de.rki.coronawarnapp.statistics.SevenDayRValue
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

fun StatsItem.getPrimaryLabel(context: Context): String {
    val today = LocalDate()
    val yesterday = today.minusDays(1)
    val updatedAtDate = LocalDate(updatedAt.toUserTimeZone())
    val dateTimeFormatter = DateTimeFormat.mediumDate().withLocale(context.getLocale())

    return when (this) {
        is InfectionStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_today)
            yesterday -> context.getString(R.string.statistics_primary_value_yesterday)
            else -> dateTimeFormatter.print(updatedAtDate)
        }
        is KeySubmissionsStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_today)
            yesterday -> context.getString(R.string.statistics_primary_value_yesterday)
            else -> dateTimeFormatter.print(updatedAtDate)
        }
        is IncidenceStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_until_today)
            yesterday -> context.getString(R.string.statistics_primary_value_until_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, dateTimeFormatter.print(updatedAtDate))
        }
        is PersonsVaccinatedOnceStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_until_today)
            yesterday -> context.getString(R.string.statistics_primary_value_until_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, dateTimeFormatter.print(updatedAtDate))
        }
        is PersonsVaccinatedCompletelyStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_until_today)
            yesterday -> context.getString(R.string.statistics_primary_value_until_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, dateTimeFormatter.print(updatedAtDate))
        }
        is AppliedVaccinationRatesStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_today)
            yesterday -> context.getString(R.string.statistics_primary_value_yesterday)
            else -> dateTimeFormatter.print(updatedAtDate)
        }
        is SevenDayRValue -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_current)
            yesterday -> context.getString(R.string.statistics_primary_value_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, dateTimeFormatter.print(updatedAtDate))
        }
        is LocalIncidenceStats -> "" // TODO No ui code done yet
    }
}
