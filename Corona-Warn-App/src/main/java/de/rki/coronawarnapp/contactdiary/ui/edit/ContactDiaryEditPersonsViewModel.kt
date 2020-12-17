package de.rki.coronawarnapp.contactdiary.ui.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.toEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
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

    val isButtonEnabled = MediatorLiveData<Boolean>().apply {
        addSource(personsLiveData) {
            value = !it.isNullOrEmpty()
        }
    }

    val isListVisible = MediatorLiveData<Boolean>().apply {
        addSource(personsLiveData) {
            value = !it.isNullOrEmpty()
        }
    }

    fun onDeleteAllPersonsClick() {
        navigationEvent.postValue(NavigationEvent.ShowDeletionConfirmationDialog)
    }

    fun onDeleteAllConfirmedClick() {
        launch {
            contactDiaryRepository.deleteAllPersonEncounters()
            contactDiaryRepository.deleteAllPeople()
        }
    }

    fun onEditPersonClick(person: ContactDiaryPerson) {
        navigationEvent.postValue(NavigationEvent.ShowPersonDetailSheet(person.toEntity()))
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryEditPersonsViewModel>

    sealed class NavigationEvent {
        object ShowDeletionConfirmationDialog: NavigationEvent()
        data class ShowPersonDetailSheet(val person: ContactDiaryPersonEntity): NavigationEvent()
    }
}
