package de.rki.coronawarnapp.contactdiary.ui.overview

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.ListItem
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import org.joda.time.LocalDate
import timber.log.Timber

class ContactDiaryOverviewViewModel @AssistedInject constructor(
    contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<ContactDiaryOverviewNavigationEvents> = SingleLiveEvent()

    private val dates = flowOf((0 until DAY_COUNT).map { LocalDate.now().minusDays(it) })

    val listItems = combine(
        dates,
        contactDiaryRepository.locationVisits,
        contactDiaryRepository.personEncounters
    ) { dateList, locationVisitList, personEncounterList ->
        createListItemList(dateList, locationVisitList, personEncounterList)
    }.asLiveData()

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
                    R.drawable.ic_contact_diary_person,
                    personEncounter.contactDiaryPerson.fullName
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
                    R.drawable.ic_contact_diary_location,
                    locationVisit.contactDiaryLocation.locationName
                )
            }
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(ContactDiaryOverviewNavigationEvents.NavigateToMainActivity)
    }

    fun onItemPress(listItem: ListItem) {
        routeToScreen.postValue(ContactDiaryOverviewNavigationEvents.NavigateToContactDiaryDayFragment(listItem.date))
    }

    fun onExportPress() {
        Timber.d("Exporting")
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryOverviewViewModel>

    companion object {
        const val DAY_COUNT = 14
    }
}
