package de.rki.coronawarnapp.contactdiary.ui.edit

import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryPersonEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ContactDiaryEditPersonsViewModel @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val contactDiaryRepository: ContactDiaryRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        launchErrorHandler = CoroutineExceptionHandler { _, ex ->
            ex.report(ExceptionCategory.INTERNAL, TAG)
        }
    }

    val navigationEvent = SingleLiveEvent<NavigationEvent>()

    val personsLiveData = contactDiaryRepository.people
        .asLiveData()

    val isButtonEnabled = contactDiaryRepository.people.map { it.isNotEmpty() }
        .asLiveData(dispatcherProvider.IO)

    val isListVisible = contactDiaryRepository.people.map { it.isNotEmpty() }
        .asLiveData(dispatcherProvider.IO)

    fun onDeleteAllPersonsClick() {
        navigationEvent.postValue(NavigationEvent.ShowDeletionConfirmationDialog)
    }

    fun onDeleteAllConfirmedClick() {
        launch(scope = appScope) {
            contactDiaryRepository.deleteAllPersonEncounters()
            contactDiaryRepository.deleteAllPeople()
        }
    }

    fun onEditPersonClick(person: ContactDiaryPerson) {
        navigationEvent.postValue(NavigationEvent.ShowPersonDetailFragment(person.toContactDiaryPersonEntity()))
    }

    sealed class NavigationEvent {
        object ShowDeletionConfirmationDialog : NavigationEvent()
        data class ShowPersonDetailFragment(val person: ContactDiaryPersonEntity) : NavigationEvent()
    }
}

private val TAG = ContactDiaryEditPersonsViewModel::class.java.simpleName
