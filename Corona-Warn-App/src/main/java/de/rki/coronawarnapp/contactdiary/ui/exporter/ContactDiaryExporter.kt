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

    suspend fun createExport(
        personEncounters: List<ContactDiaryPersonEncounter>,
        locationVisits: List<ContactDiaryLocationVisit>,
        numberOfLastDaysToExport: Int
    ): String = withContext(dispatcherProvider.Default) {

        val groupedPersonEncounters = personEncounters.groupBy({ it.date }, { it.contactDiaryPerson.fullName })
        val groupedLocationVisits = locationVisits.groupBy({ it.date }, { it.contactDiaryLocation.locationName })

        val datesToExport = (0 until numberOfLastDaysToExport).map { timeStamper.nowUTC.toLocalDate().minusDays(it) }

        val sb = StringBuilder()
            .appendLine(
                context.getString(
                    R.string.contact_diary_export_intro_one,
                    datesToExport.last().toFormattedString(),
                    datesToExport.first().toFormattedString()
                )
            )
            .appendLine(context.getString(R.string.contact_diary_export_intro_two))
            .appendLine()

        for (date in datesToExport) {
            val dateString = date.toFormattedString()

            // According to tech spec persons first and then locations
            groupedPersonEncounters[date]?.addToStringBuilder(sb, dateString)
            groupedLocationVisits[date]?.addToStringBuilder(sb, dateString)
        }

        sb.toString()
    }

    // According to tech spec german locale only
    private fun LocalDate.toFormattedString(): String = toString("dd.MM.yyyy", Locale.GERMAN)

    private fun List<String>.addToStringBuilder(sb: StringBuilder, dateString: String) = sortedBy {
        it.toLowerCase(Locale.ROOT)
    }.forEach { sb.appendLine("$dateString $it") }
}
