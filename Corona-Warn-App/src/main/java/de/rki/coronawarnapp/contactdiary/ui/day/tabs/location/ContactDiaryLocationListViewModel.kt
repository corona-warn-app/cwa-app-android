package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.Location
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.util.SelectableItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import org.joda.time.Instant

class ContactDiaryLocationListViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted selectedDay: Long,
    contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val dayElement = contactDiaryRepository.filterForDay(Instant.ofEpochMilli(selectedDay))
    private val selectableLocations = MutableStateFlow<List<SelectableItem<Location>>>(emptyList())

    init {
        launch {
            dayElement.collect {
                selectableLocations.emit(
                    it.locations.map { location -> SelectableItem(false, location) }
                )
            }
        }
    }

    val locations = selectableLocations.asLiveData()

    fun locationSelectionChanged(item: SelectableItem<Location>) = launch {
        val newLocations = selectableLocations.value.map {
            if (it.stableId == item.stableId) {
                SelectableItem(!item.selected, item.item)
            } else {
                it
            }
        }
        selectableLocations.emit(newLocations)
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryLocationListViewModel> {
        fun create(selectedDay: Long): ContactDiaryLocationListViewModel
    }
}
