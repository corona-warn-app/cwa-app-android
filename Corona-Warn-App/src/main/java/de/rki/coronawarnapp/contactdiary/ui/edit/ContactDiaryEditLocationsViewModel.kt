package de.rki.coronawarnapp.contactdiary.ui.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.toEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ContactDiaryEditLocationsViewModel @AssistedInject constructor(
    private val contactDiaryRepository: ContactDiaryRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider){

    val locationsLiveData = contactDiaryRepository.locations.asLiveData()

    val navigationEvent = SingleLiveEvent<NavigationEvent>()

    val isButtonEnabled = MediatorLiveData<Boolean>().apply {
        addSource(locationsLiveData) {
            value = !it.isNullOrEmpty()
        }
    }

    val isListVisible = MediatorLiveData<Boolean>().apply {
        addSource(locationsLiveData) {
            value = !it.isNullOrEmpty()
        }
    }

    fun onDeleteAllLocationsClick() {
        navigationEvent.postValue(NavigationEvent.ShowDeletionConfirmationDialog)
    }

    fun onDeleteAllConfirmedClick() {
        launch {
            contactDiaryRepository.deleteAllLocationVisits()
            contactDiaryRepository.deleteAllLocations()
        }
    }


    fun onEditLocationClick(location: ContactDiaryLocation) {
        navigationEvent.postValue(NavigationEvent.ShowLocationDetailSheet(location.toEntity()))
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryEditLocationsViewModel>

    sealed class NavigationEvent {
        object ShowDeletionConfirmationDialog: NavigationEvent()
        data class ShowLocationDetailSheet(val location: ContactDiaryLocationEntity): NavigationEvent()
    }
}
