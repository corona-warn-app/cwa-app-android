package de.rki.coronawarnapp.contactdiary.ui.overview

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DrawableAndString
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

    private val dates = flowOf((0..13).map { LocalDate.now().minusDays(it) })

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
        Timber.v("createListItemList(dateList=$dateList, locationVisitList=$locationVisitList, personEncounterList=$personEncounterList)")
        return dateList
            .map {
                ListItem(it)
                    .apply {
                        drawableAndStrings.addPersonEncountersForDate(personEncounterList, date)
                        drawableAndStrings.addLocationVisitsForDate(locationVisitList, date)
                    }
            }
    }

    private fun MutableList<DrawableAndString>.addPersonEncountersForDate(
        personEncounterList: List<ContactDiaryPersonEncounter>,
        date: LocalDate
    ) {
        this += personEncounterList
            .filter { personEncounter -> personEncounter.date == date }
            .map { personEncounter ->
                DrawableAndString(
                    R.drawable.ic_contact_diary_person,
                    personEncounter.contactDiaryPerson.fullName
                )
            }
    }

    private fun MutableList<DrawableAndString>.addLocationVisitsForDate(
        locationVisitList: List<ContactDiaryLocationVisit>,
        date: LocalDate
    ) {
        this += locationVisitList
            .filter { locationVisit -> locationVisit.date == date }
            .map { locationVisit ->
                DrawableAndString(
                    R.drawable.ic_contact_diary_location,
                    locationVisit.contactDiaryLocation.locationName
                )
            }
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(ContactDiaryOverviewNavigationEvents.NavigateToMainActivity)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryOverviewViewModel>
}
