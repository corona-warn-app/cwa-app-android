package de.rki.coronawarnapp.ui.submission.viewmodel

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionDispatcherViewModel @AssistedInject constructor() : CWAViewModel() {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    fun onBackPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
    }

    fun onTanPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToTAN)
    }

    fun onTeleTanPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToContact)
    }

    fun onQRCodePressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToConsent)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDispatcherViewModel>
}
