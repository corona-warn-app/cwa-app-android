package de.rki.coronawarnapp.contactdiary.ui.edit

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryPersonEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class ContactDiaryEditPersonsViewModel @AssistedInject constructor(
    private val contactDiaryRepository: ContactDiaryRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val personsLiveData = contactDiaryRepository.people.asLiveData()

    val navigationEvent = SingleLiveEvent<NavigationEvent>()

    val isButtonEnabled = contactDiaryRepository.people.map { !it.isNullOrEmpty() }.asLiveData()

    val isListVisible = contactDiaryRepository.people.map { !it.isNullOrEmpty() }.asLiveData()

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
        navigationEvent.postValue(NavigationEvent.ShowPersonDetailSheet(person.toContactDiaryPersonEntity()))
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryEditPersonsViewModel>

    sealed class NavigationEvent {
        object ShowDeletionConfirmationDialog : NavigationEvent()
        data class ShowPersonDetailSheet(val person: ContactDiaryPersonEntity) : NavigationEvent()
    }
}
