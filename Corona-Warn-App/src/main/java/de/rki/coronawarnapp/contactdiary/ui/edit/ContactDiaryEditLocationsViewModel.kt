package de.rki.coronawarnapp.contactdiary.ui.edit

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.map

class ContactDiaryEditLocationsViewModel @AssistedInject constructor(
    private val contactDiaryRepository: ContactDiaryRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, ex ->
        ex.report(ExceptionCategory.INTERNAL, TAG)
    }

    val locationsLiveData = contactDiaryRepository.locations
        .asLiveData(dispatcherProvider.IO)

    val navigationEvent = SingleLiveEvent<NavigationEvent>()

    val isButtonEnabled = contactDiaryRepository.locations.map { it.isNotEmpty() }
        .asLiveData(dispatcherProvider.IO)

    val isListVisible = contactDiaryRepository.locations.map { it.isNotEmpty() }
        .asLiveData(dispatcherProvider.IO)

    fun onDeleteAllLocationsClick() {
        navigationEvent.postValue(NavigationEvent.ShowDeletionConfirmationDialog)
    }

    fun onDeleteAllConfirmedClick() {
        launch(coroutineExceptionHandler) {
            contactDiaryRepository.deleteAllLocationVisits()
            contactDiaryRepository.deleteAllLocations()
        }
    }

    fun onEditLocationClick(location: ContactDiaryLocation) {
        navigationEvent.postValue(NavigationEvent.ShowLocationDetailSheet(location.toContactDiaryLocationEntity()))
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryEditLocationsViewModel>

    sealed class NavigationEvent {
        object ShowDeletionConfirmationDialog : NavigationEvent()
        data class ShowLocationDetailSheet(val location: ContactDiaryLocationEntity) : NavigationEvent()
    }
}

private val TAG = ContactDiaryEditLocationsViewModel::class.java.simpleName
