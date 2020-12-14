package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.util.SelectableItem
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.joda.time.Instant

class ContactDiaryPersonListViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted selectedDay: Long,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val localDate = Instant.ofEpochSecond(selectedDay).toLocalDate()

    private val dayElement = contactDiaryRepository.personEncountersForDate(localDate)
    private val selectablePersons = contactDiaryRepository.people

    val uiList = selectablePersons.combine(dayElement) { persons, dayElement ->
        persons.map { contactDiaryPerson ->
            if (dayElement.any { it.contactDiaryPerson.personId == contactDiaryPerson.personId }) {
                SelectableItem(true, contactDiaryPerson)
            } else {
                SelectableItem(false, contactDiaryPerson)
            }
        }
    }.asLiveData()

    fun personSelectionChanged(item: SelectableItem<ContactDiaryPerson>) = launch {
        if (!item.selected) {
            contactDiaryRepository.addPersonEncounter(
                DefaultContactDiaryPersonEncounter(
                    date = localDate,
                    contactDiaryPerson = item.item
                )
            )
        } else {
            val visit = dayElement.first()
                .find { it.contactDiaryPerson.personId == item.item.personId }
            visit?.let { contactDiaryRepository.deletePersonEncounter(it) }
        }
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryPersonListViewModel> {
        fun create(selectedDay: Long): ContactDiaryPersonListViewModel
    }
}
