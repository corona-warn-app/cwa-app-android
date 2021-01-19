package de.rki.coronawarnapp.contactdiary.ui.overview

import android.content.Context
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryCleanTask
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.ListItem
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.joda.time.LocalDate
import timber.log.Timber
import java.util.Locale

class ContactDiaryOverviewViewModel @AssistedInject constructor(
    taskController: TaskController,
    dispatcherProvider: DispatcherProvider,
    contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<ContactDiaryOverviewNavigationEvents> = SingleLiveEvent()
    val exportLocationsAndPersons: SingleLiveEvent<String> = SingleLiveEvent()

    private val dates = (0 until DAY_COUNT).map { LocalDate.now().minusDays(it) }

    private val locationVisitsFlow = contactDiaryRepository.locationVisits
    private val personEncountersFlow = contactDiaryRepository.personEncounters

    val listItems = combine(
        flowOf(dates),
        locationVisitsFlow,
        personEncountersFlow
    ) { dateList, locationVisitList, personEncounterList ->
        createListItemList(dateList, locationVisitList, personEncounterList)
    }.asLiveData()

    init {
        taskController.submit(
            DefaultTaskRequest(
                ContactDiaryCleanTask::class,
                originTag = "ContactDiaryOverviewViewModelInit"
            )
        )
    }

    private fun createListItemList(
        dateList: List<LocalDate>,
        locationVisitList: List<ContactDiaryLocationVisit>,
        personEncounterList: List<ContactDiaryPersonEncounter>
    ): List<ListItem> {
        Timber.v(
            "createListItemList(dateList=$dateList, " +
                "locationVisitList=$locationVisitList, " +
                "personEncounterList=$personEncounterList)"
        )
        return dateList
            .map {
                ListItem(it)
                    .apply {
                        data.addPersonEncountersForDate(personEncounterList, date)
                        data.addLocationVisitsForDate(locationVisitList, date)
                    }
            }
    }

    private fun MutableList<ListItem.Data>.addPersonEncountersForDate(
        personEncounterList: List<ContactDiaryPersonEncounter>,
        date: LocalDate
    ) {
        this += personEncounterList
            .filter { personEncounter -> personEncounter.date == date }
            .map { personEncounter ->
                ListItem.Data(
                    R.drawable.ic_contact_diary_person_item,
                    personEncounter.contactDiaryPerson.fullName,
                    ListItem.Type.PERSON
                )
            }
    }

    private fun MutableList<ListItem.Data>.addLocationVisitsForDate(
        locationVisitList: List<ContactDiaryLocationVisit>,
        date: LocalDate
    ) {
        this += locationVisitList
            .filter { locationVisit -> locationVisit.date == date }
            .map { locationVisit ->
                ListItem.Data(
                    R.drawable.ic_contact_diary_location_item,
                    locationVisit.contactDiaryLocation.locationName,
                    ListItem.Type.LOCATION
                )
            }
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(ContactDiaryOverviewNavigationEvents.NavigateToMainActivity)
    }

    fun onItemPress(listItem: ListItem) {
        routeToScreen.postValue(ContactDiaryOverviewNavigationEvents.NavigateToContactDiaryDayFragment(listItem.date))
    }

    fun onExportPress(ctx: Context) {
        Timber.d("Exporting person and location entries")
        launch {
            val locationVisits = locationVisitsFlow
                .first()
                .groupBy({ it.date }, { it.contactDiaryLocation.locationName })

            val personEncounters = personEncountersFlow
                .first()
                .groupBy({ it.date }, { it.contactDiaryPerson.fullName })

            val sb = StringBuilder()
                .appendLine(ctx.getString(R.string.contact_diary_export_intro_one,
                    dates.last().toFormattedString(),
                    dates.first().toFormattedString()))
                .appendLine(ctx.getString(R.string.contact_diary_export_intro_two))
                .appendLine()

            for (date in dates) {
                val dateString = date.toFormattedString()

                // According to tech spec persons first and then locations
                personEncounters[date]?.addToStringBuilder(sb, dateString)
                locationVisits[date]?.addToStringBuilder(sb, dateString)
            }

            exportLocationsAndPersons.postValue(sb.toString())
        }
    }

    private fun List<String>.addToStringBuilder(sb: StringBuilder, dateString: String) = sortedBy {
        it.toLowerCase(Locale.ROOT)
    }
        .forEach { sb.appendLine("$dateString $it") }

    // According to tech spec german locale only
    private fun LocalDate.toFormattedString(): String = toString("dd.MM.yyyy", Locale.GERMAN)

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryOverviewViewModel>

    companion object {
        const val DAY_COUNT = 14
    }
}
