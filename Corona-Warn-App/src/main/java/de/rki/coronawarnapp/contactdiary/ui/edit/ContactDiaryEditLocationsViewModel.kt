package de.rki.coronawarnapp.contactdiary.ui.edit

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
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

    fun onDeleteAllLocationsClick() {
        navigationEvent.postValue(NavigationEvent.ShowDeletionConfirmationDialog)
    }

    fun onDeleteAllConfirmedClick() {
        launch {
            contactDiaryRepository.deleteAllLocations()
        }
    }

    fun onEditLocationClick(id: Int) {
        navigationEvent.postValue(NavigationEvent.ShowLocationDetailSheet(id))
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryEditLocationsViewModel>

    sealed class NavigationEvent {
        object ShowDeletionConfirmationDialog: NavigationEvent()
        data class ShowLocationDetailSheet(val id: Int): NavigationEvent()
    }
}
