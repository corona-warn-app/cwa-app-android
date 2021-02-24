package de.rki.coronawarnapp.contactdiary.ui.exporter

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.withContext
import org.joda.time.Duration
import org.joda.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactDiaryExporter @Inject constructor(
    @AppContext private val context: Context,
    private val timeStamper: TimeStamper,
    private val dispatcherProvider: DispatcherProvider
) {

    private val prefixPhone = context.getString(R.string.contact_diary_export_prefix_phone)
    private val prefixEMail = context.getString(R.string.contact_diary_export_prefix_email)

    private val textDurationLessThan15Min = context.getString(R.string.contact_diary_export_durations_less_than_15min)
    private val textDurationLongerThan15Min =
        context.getString(R.string.contact_diary_export_durations_longer_than_15min)

    private val textWithMask = context.getString(R.string.contact_diary_export_wearing_mask)
    private val textNoMask = context.getString(R.string.contact_diary_export_wearing_no_mask)

    private val textWasOutdoors = context.getString(R.string.contact_diary_export_outdoor)
    private val textWasIndoor = context.getString(R.string.contact_diary_export_indoor)

    private val durationPrefix = context.getString(R.string.contact_diary_export_location_duration_prefix)
    private val durationSuffix = context.getString(R.string.contact_diary_export_location_duration_suffix)

    suspend fun createExport(
        personEncounters: List<ContactDiaryPersonEncounter>,
        locationVisits: List<ContactDiaryLocationVisit>,
        numberOfLastDaysToExport: Int
    ): String = withContext(dispatcherProvider.Default) {

        val datesToExport = generateDatesToExport(numberOfLastDaysToExport)

        StringBuilder()
            .appendIntro(datesToExport)
            .appendPersonsAndLocations(personEncounters, locationVisits, datesToExport)
            .toString()
    }

    private fun generateDatesToExport(numberOfLastDaysToExport: Int) =
        (0 until numberOfLastDaysToExport).map { timeStamper.nowUTC.toLocalDate().minusDays(it) }

    private fun StringBuilder.appendIntro(datesToExport: List<LocalDate>) = apply {
        appendLine(
            context.getString(
                R.string.contact_diary_export_intro_one,
                datesToExport.last().toFormattedString(),
                datesToExport.first().toFormattedString()
            )
        )
        appendLine(context.getString(R.string.contact_diary_export_intro_two))
    }

    private fun StringBuilder.appendPersonsAndLocations(
        personEncounters: List<ContactDiaryPersonEncounter>,
        locationVisits: List<ContactDiaryLocationVisit>,
        datesToExport: List<LocalDate>
    ) = apply {

        if (personEncounters.isNotEmpty() || locationVisits.isNotEmpty()) {
            appendLine()
        } else {
            return this
        }

        val groupedPersonEncounters = personEncounters.groupBy { it.date }
        val groupedLocationVisits = locationVisits.groupBy { it.date }

        for (date in datesToExport) {

            // According to tech spec persons first and then locations
            groupedPersonEncounters[date]
                ?.sortedBy { getStringToSortBy(it.contactDiaryPerson.fullName) }
                ?.map { it.getExportInfo(it.date) }
                ?.forEach { appendLine(it) }

            groupedLocationVisits[date]
                ?.sortedBy { getStringToSortBy(it.contactDiaryLocation.locationName) }
                ?.map { it.getExportInfo(it.date) }
                ?.forEach { appendLine(it) }
        }

        return this
    }

    private fun getStringToSortBy(name: String) = name.toLowerCase(Locale.ROOT)

    private fun ContactDiaryPersonEncounter.getExportInfo(date: LocalDate) = listOfNotNull(
        getDateAndNameString(contactDiaryPerson.fullName, date),
        contactDiaryPerson.phoneNumber?.let { getPhoneWithPrefix(it) },
        contactDiaryPerson.emailAddress?.let { getEMailWithPrefix(it) },
        durationClassification?.let { getDurationClassificationString(it) },
        withMask?.let { if (it) textWithMask else textNoMask },
        wasOutside?.let { if (it) textWasOutdoors else textWasIndoor },
        circumstances
    ).joinToString(separator = "; ")

    private fun ContactDiaryLocationVisit.getExportInfo(date: LocalDate): String {
        return listOfNotNull(
            getDateAndNameString(contactDiaryLocation.locationName, date),
            contactDiaryLocation.phoneNumber?.let { getPhoneWithPrefix(it) },
            contactDiaryLocation.emailAddress?.let { getEMailWithPrefix(it) },
            getReadableDuration(duration),
            circumstances
        ).joinToString(separator = "; ")
    }

    private fun getDateAndNameString(name: String, date: LocalDate) = "${date.toFormattedString()} $name"

    private fun getPhoneWithPrefix(phone: String) = if (phone.isNotBlank()) {
        "$prefixPhone $phone"
    } else null

    private fun getEMailWithPrefix(eMail: String) = if (eMail.isNotBlank()) {
        "$prefixEMail $eMail"
    } else null

    private fun getDurationClassificationString(duration: ContactDiaryPersonEncounter.DurationClassification) =
        when (duration) {
            ContactDiaryPersonEncounter.DurationClassification.LESS_THAN_15_MINUTES -> textDurationLessThan15Min
            ContactDiaryPersonEncounter.DurationClassification.MORE_THAN_15_MINUTES -> textDurationLongerThan15Min
        }

    // According to tech spec german locale only
    private fun LocalDate.toFormattedString(): String = toString("dd.MM.yyyy", Locale.GERMAN)

    // returns readable durations as e.g. "Dauer 01:30 h"
    private fun getReadableDuration(duration: Duration?): String? {
        if (duration == null) return null

        val durationInMinutes = duration.standardMinutes
        val durationString = String.format("%02d:%02d", durationInMinutes / 60, (durationInMinutes % 60))

        return "$durationPrefix $durationString $durationSuffix"
    }
}
