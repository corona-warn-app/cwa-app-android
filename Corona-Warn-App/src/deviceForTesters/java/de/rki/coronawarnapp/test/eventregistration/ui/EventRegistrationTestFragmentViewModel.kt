package de.rki.coronawarnapp.test.eventregistration.ui

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.CheckInMatcher
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class EventRegistrationTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val checkInMatcher: CheckInMatcher
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val checkInOverlaps = checkInMatcher.eventOverlapFlow.asLiveData()

    fun runMatcher() {
        launch { checkInMatcher.execute() }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<EventRegistrationTestFragmentViewModel>
}
