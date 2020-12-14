package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.util.SelectableItem
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.joda.time.Instant

class ContactDiaryLocationListViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted selectedDay: Long,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val localDate = Instant.ofEpochSecond(selectedDay).toLocalDate()

    private val dayElement = contactDiaryRepository.locationVisitsForDate(localDate)
    private val selectableLocations = contactDiaryRepository.locations

    val uiList = selectableLocations.combine(dayElement) { locations, dayElement ->
        locations.map { contactDiaryLocation ->
            if (dayElement.any { it.contactDiaryLocation.locationId == contactDiaryLocation.locationId }) {
                SelectableItem(true, contactDiaryLocation)
            } else {
                SelectableItem(false, contactDiaryLocation)
            }
        }
    }.asLiveData()

    fun locationSelectionChanged(item: SelectableItem<ContactDiaryLocation>) = launch {
        if (!item.selected) {
            contactDiaryRepository.addLocationVisit(
                DefaultContactDiaryLocationVisit(
                    date = localDate,
                    contactDiaryLocation = item.item
                )
            )
        } else {
            val visit = dayElement.first()
                .find { it.contactDiaryLocation.locationId == item.item.locationId }
            visit?.let { contactDiaryRepository.deleteLocationVisit(it) }
        }
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryLocationListViewModel> {
        fun create(selectedDay: Long): ContactDiaryLocationListViewModel
    }
}
