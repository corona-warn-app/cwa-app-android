package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.Person
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.util.SelectableItem
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import org.joda.time.Instant

class ContactDiaryPersonListViewModel @AssistedInject constructor(
    @Assisted selectedDay: Long,
    contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel() {

    private val dayElement = contactDiaryRepository.filterForDay(Instant.ofEpochMilli(selectedDay))
    private val selectablePersons = MutableStateFlow<List<SelectableItem<Person>>>(emptyList())

    init {
        launch {
            dayElement.collect {
                selectablePersons.emit(
                    it.people.map { person -> SelectableItem(false, person) }
                )
            }
        }
    }

    val persons = selectablePersons.asLiveData()

    fun personSelectionChanged(item: SelectableItem<Person>) = launch {
        val newPersons = selectablePersons.value.map {
            if (it.stableId == item.stableId) {
                SelectableItem(!item.selected, item.item)
            } else {
                it
            }
        }
        selectablePersons.emit(newPersons)
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryPersonListViewModel> {
        fun create(selectedDay: Long): ContactDiaryPersonListViewModel
    }
}
