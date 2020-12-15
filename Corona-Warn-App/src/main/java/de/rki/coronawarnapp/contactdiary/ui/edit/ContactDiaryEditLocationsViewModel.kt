package de.rki.coronawarnapp.contactdiary.ui.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ContactDiaryEditLocationsViewModel @AssistedInject constructor(
    private val contactDiaryRepository: ContactDiaryRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider){

    val locationsLiveData : MutableLiveData<List<ContactDiaryLocation>> = MutableLiveData(listOf(
        DefaultContactDiaryLocation(1, "1st location"),
            DefaultContactDiaryLocation(2, "2nd location"),
        DefaultContactDiaryLocation(3, "very long name that never ends and goes on and on and on")))
        //contactDiaryRepository.locations.asLiveData()

    val navigationEvent = SingleLiveEvent<NavigationEvent>()

    val isButtonEnabled = MediatorLiveData<Boolean>().apply {
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

    fun delete(location: ContactDiaryLocation) {
        val list = locationsLiveData.value?.toMutableList()
        list?.remove(location)
        locationsLiveData.postValue(list)
    }

    fun onEditLocationClick(location: ContactDiaryLocation) {
        navigationEvent.postValue(NavigationEvent.ShowLocationDetailSheet(location))
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryEditLocationsViewModel>

    sealed class NavigationEvent {
        object ShowDeletionConfirmationDialog: NavigationEvent()
        data class ShowLocationDetailSheet(val location: ContactDiaryLocation): NavigationEvent()
    }
}
