package de.rki.coronawarnapp.contactdiary.ui.day.sheets.location

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class ContactDiaryLocationBottomSheetDialogViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val text = MutableStateFlow("")

    val isValid = text.map {
        it.isNotEmpty() && it.length <= MAX_LOCATION_NAME_LENGTH
    }.asLiveData()

    val shouldClose = SingleLiveEvent<Unit>()

    fun textChanged(locationName: String) {
        text.value = locationName
    }

    fun saveLocation() = launch {
        contactDiaryRepository.addLocation(
            DefaultContactDiaryLocation(
                locationName = text.value.take(MAX_LOCATION_NAME_LENGTH)
            )
        )
        shouldClose.postValue(null)
    }

    fun closePressed() {
        shouldClose.postValue(null)
    }

    companion object {
        private const val MAX_LOCATION_NAME_LENGTH = 250
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryLocationBottomSheetDialogViewModel>
}
