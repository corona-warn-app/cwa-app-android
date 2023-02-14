package de.rki.coronawarnapp.contactdiary.ui.edit

import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryLocationEntity
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
class ContactDiaryEditLocationsViewModel @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val contactDiaryRepository: ContactDiaryRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        launchErrorHandler = CoroutineExceptionHandler { _, ex ->
            ex.report(ExceptionCategory.INTERNAL, TAG)
        }
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
        launch(scope = appScope) {
            contactDiaryRepository.deleteAllLocationVisits()
            contactDiaryRepository.deleteAllLocations()
        }
    }

    fun onEditLocationClick(location: ContactDiaryLocation) {
        navigationEvent.postValue(NavigationEvent.ShowLocationDetailFragment(location.toContactDiaryLocationEntity()))
    }

    sealed class NavigationEvent {
        object ShowDeletionConfirmationDialog : NavigationEvent()
        data class ShowLocationDetailFragment(val location: ContactDiaryLocationEntity) : NavigationEvent()
    }
}

private val TAG = ContactDiaryEditLocationsViewModel::class.java.simpleName
