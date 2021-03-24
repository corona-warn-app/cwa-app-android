package de.rki.coronawarnapp.test.eventregistration.ui

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.CheckInMatcher
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.CheckInOverlap
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class EventRegistrationTestFragmentViewModel @AssistedInject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val checkInMatcher: CheckInMatcher
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val checkInOverlaps = MutableLiveData<List<CheckInOverlap>>()

    fun runMatcher() {
        launch {
            val overlaps = checkInMatcher.execute()
            checkInOverlaps.postValue(overlaps)
        }
    }

    fun downloadQrCodePosterTemplate() {
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<EventRegistrationTestFragmentViewModel>
}
