package de.rki.coronawarnapp.ui.submission.viewmodel

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionContactViewModel @AssistedInject constructor() : CWAViewModel() {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()
    val dial = SingleLiveEvent<Unit>()

    fun onBackPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDispatcher)
    }

    fun onDialPressed() {
        dial.postValue(Unit)
    }

    fun onEnterTanPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToTAN)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionContactViewModel>
}
