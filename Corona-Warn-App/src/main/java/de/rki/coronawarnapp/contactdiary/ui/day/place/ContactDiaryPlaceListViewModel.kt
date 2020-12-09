package de.rki.coronawarnapp.contactdiary.ui.day.place

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import org.joda.time.Instant

class ContactDiaryPlaceListViewModel @AssistedInject constructor(
    @Assisted selectedDay: Long,
    contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel() {

    private val dayElement = contactDiaryRepository.filterForDay(Instant.ofEpochMilli(selectedDay))

    val locations = dayElement.map { it.locations }.asLiveData()

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryPlaceListViewModel> {
        fun create(selectedDay: Long): ContactDiaryPlaceListViewModel
    }
}
