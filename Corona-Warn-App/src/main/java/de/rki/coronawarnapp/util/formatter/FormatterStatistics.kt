package de.rki.coronawarnapp.util.formatter

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.datadonation.analytics.common.labelStringRes
import de.rki.coronawarnapp.statistics.AppliedVaccinationRatesStats
import de.rki.coronawarnapp.statistics.GlobalStatsItem
import de.rki.coronawarnapp.statistics.IncidenceAndHospitalizationStats
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.LocalIncidenceAndHospitalizationStats
import de.rki.coronawarnapp.statistics.LocalStatsItem
import de.rki.coronawarnapp.statistics.OccupiedIntensiveCareStats
import de.rki.coronawarnapp.statistics.PersonsVaccinatedCompletelyStats
import de.rki.coronawarnapp.statistics.PersonsVaccinatedOnceStats
import de.rki.coronawarnapp.statistics.PersonsVaccinatedWithBoosterStats
import de.rki.coronawarnapp.statistics.SevenDayRValue
import de.rki.coronawarnapp.statistics.local.storage.SelectedStatisticsLocation
import de.rki.coronawarnapp.util.toLocalDateUserTz
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun GlobalStatsItem.getPrimaryLabel(context: Context): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val updatedAtDate = updatedAt.toLocalDateUserTz()
    val dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(context.getLocale())

    return when (this) {
        is InfectionStats,
        is KeySubmissionsStats,
        is AppliedVaccinationRatesStats,
        is OccupiedIntensiveCareStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_today)
            yesterday -> context.getString(R.string.statistics_primary_value_yesterday)
            else -> updatedAtDate.format(dateTimeFormatter)
        }
        is PersonsVaccinatedOnceStats,
        is IncidenceAndHospitalizationStats,
        is PersonsVaccinatedCompletelyStats,
        is PersonsVaccinatedWithBoosterStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_until_today)
            yesterday -> context.getString(R.string.statistics_primary_value_until_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, updatedAtDate.format(dateTimeFormatter))
        }
        is SevenDayRValue -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_current)
            yesterday -> context.getString(R.string.statistics_primary_value_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, updatedAtDate.format(dateTimeFormatter))
        }
    }
}

fun getSecondaryLabel(context: Context, updatedAt: Instant): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val updatedAtDate = updatedAt.toLocalDateUserTz()
    val dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(context.getLocale())

    return when (updatedAtDate) {
        today -> context.getString(R.string.statistics_primary_value_until_today)
        yesterday -> context.getString(R.string.statistics_primary_value_until_yesterday)
        else -> context.getString(R.string.statistics_primary_value_until, updatedAtDate.format(dateTimeFormatter))
    }
}

fun LocalStatsItem.getPrimaryLabel(context: Context, localUpdatedAt: Instant = updatedAt): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val updatedAtDate = localUpdatedAt.toLocalDateUserTz()
    val dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(context.getLocale())

    return when (this) {
        is LocalIncidenceAndHospitalizationStats -> when (updatedAtDate) {
            today -> context.getString(R.string.statistics_primary_value_until_today)
            yesterday -> context.getString(R.string.statistics_primary_value_until_yesterday)
            else -> context.getString(R.string.statistics_primary_value_until, updatedAtDate.format(dateTimeFormatter))
        }
    }
}

fun LocalIncidenceAndHospitalizationStats.getLocationLabel(context: Context): String {
    return when (selectedLocation) {
        is SelectedStatisticsLocation.SelectedDistrict ->
            selectedLocation.district.districtName
        is SelectedStatisticsLocation.SelectedFederalState ->
            context.getString(selectedLocation.federalState.labelStringRes)
    }
}

fun LocalIncidenceAndHospitalizationStats.getDistrictLabel(context: Context): String {
    return when (selectedLocation) {
        is SelectedStatisticsLocation.SelectedDistrict ->
            context.getString(R.string.statistics_card_local_hospitalization_text).format(
                when (selectedLocation.district.federalStateShortName) {
                    "BW" -> context.getString(R.string.analytics_userinput_federalstate_bw)
                    "BY" -> context.getString(R.string.analytics_userinput_federalstate_by)
                    "BE" -> context.getString(R.string.analytics_userinput_federalstate_be)
                    "BB" -> context.getString(R.string.analytics_userinput_federalstate_bb)
                    "HB" -> context.getString(R.string.analytics_userinput_federalstate_hb)
                    "HH" -> context.getString(R.string.analytics_userinput_federalstate_hh)
                    "HE" -> context.getString(R.string.analytics_userinput_federalstate_he)
                    "MV" -> context.getString(R.string.analytics_userinput_federalstate_mv)
                    "NI" -> context.getString(R.string.analytics_userinput_federalstate_ni)
                    "NW" -> context.getString(R.string.analytics_userinput_federalstate_nrw)
                    "RP" -> context.getString(R.string.analytics_userinput_federalstate_rp)
                    "SL" -> context.getString(R.string.analytics_userinput_federalstate_sl)
                    "SN" -> context.getString(R.string.analytics_userinput_federalstate_sn)
                    "ST" -> context.getString(R.string.analytics_userinput_federalstate_st)
                    "SH" -> context.getString(R.string.analytics_userinput_federalstate_sh)
                    "TH" -> context.getString(R.string.analytics_userinput_federalstate_th)
                    else -> context.getString(R.string.statistics_nationwide_text)
                }
            )
        is SelectedStatisticsLocation.SelectedFederalState ->
            context.getString(R.string.statistics_secondary_value_description)
    }
}
