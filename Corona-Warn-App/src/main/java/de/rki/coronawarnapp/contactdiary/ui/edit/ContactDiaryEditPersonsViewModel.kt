package de.rki.coronawarnapp.contactdiary.ui.edit

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ContactDiaryEditPersonsViewModel @AssistedInject constructor(
    private val contactDiaryRepository: ContactDiaryRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider){

    val personsLiveData = contactDiaryRepository.people.asLiveData()

    val navigationEvent = SingleLiveEvent<NavigationEvent>()

    fun onDeleteAllPersonsClick() {
        navigationEvent.postValue(NavigationEvent.ShowDeletionConfirmationDialog)
    }

    fun onDeleteAllConfirmedClick() {
        launch {
            contactDiaryRepository.deleteAllPeople()
        }
    }

    fun onEditLocationClick(id: Int) {
        navigationEvent.postValue(NavigationEvent.ShowPersonDetailSheet(id))
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryEditPersonsViewModel>

    sealed class NavigationEvent {
        object ShowDeletionConfirmationDialog: NavigationEvent()
        data class ShowPersonDetailSheet(val id: Int): NavigationEvent()
    }
}
