package de.rki.coronawarnapp.contactdiary.ui.sheets.location

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate

class ContactDiaryLocationBottomSheetDialogViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val addedAt: String?,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val text = MutableStateFlow("")

    val isValid = text.map {
        it.isNotEmpty()
    }.asLiveData()

    val shouldClose = SingleLiveEvent<Unit>()

    private val formattedName: String
        get() {
            var newName = text.value
            //allow only spaces as a name
            if (newName.isNotBlank()) {
                newName = newName.trim()
            }
            return newName.take(MAX_LOCATION_NAME_LENGTH)
        }

    fun textChanged(locationName: String) {
        text.value = locationName
    }

    fun addLocation() = launch {
        val location = contactDiaryRepository.addLocation(
            DefaultContactDiaryLocation(
                locationName = formattedName
            )
        )

        addedAt?.let {
            contactDiaryRepository.addLocationVisit(
                DefaultContactDiaryLocationVisit(
                    date = LocalDate.parse(it),
                    contactDiaryLocation = location
                )
            )
        }

        shouldClose.postValue(null)
    }

    fun updateLocation(location: ContactDiaryLocationEntity) = launch {
        contactDiaryRepository.updateLocation(
            DefaultContactDiaryLocation(
                location.locationId,
                locationName = formattedName
            )
        )
        shouldClose.postValue(null)
    }

    fun deleteLocation(location: ContactDiaryLocationEntity) = launch {
        contactDiaryRepository.locationVisits.firstOrNull()?.forEach {
            if (it.contactDiaryLocation.locationId == location.locationId)
                contactDiaryRepository.deleteLocationVisit(it)
        }
        contactDiaryRepository.deleteLocation(location)
        shouldClose.postValue(null)
    }

    fun closePressed() {
        shouldClose.postValue(null)
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryLocationBottomSheetDialogViewModel> {
        fun create(addedAt: String?): ContactDiaryLocationBottomSheetDialogViewModel
    }
}

private const val MAX_LOCATION_NAME_LENGTH = 250
