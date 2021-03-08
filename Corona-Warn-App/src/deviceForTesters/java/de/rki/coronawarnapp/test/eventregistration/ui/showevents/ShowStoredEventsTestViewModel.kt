package de.rki.coronawarnapp.test.eventregistration.ui.showevents

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.storage.repo.HostedEventRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ShowStoredEventsTestViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val hostedEventRepository: HostedEventRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ShowStoredEventsTestViewModel>

    val storedEvents = hostedEventRepository.allHostedEvents.asLiveData()

    fun deleteAllEvents() {
        launch {
            hostedEventRepository.deleteAllHostedEvents()
        }
    }
}
