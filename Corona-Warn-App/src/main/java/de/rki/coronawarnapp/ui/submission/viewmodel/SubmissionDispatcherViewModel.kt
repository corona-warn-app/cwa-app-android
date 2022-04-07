package de.rki.coronawarnapp.ui.submission.viewmodel

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettingsDataStore
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first

class SubmissionDispatcherViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettingsDataStore,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

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
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToQRCodeScan)
    }

    fun onRatProfilePressed() = launch {
        routeToScreen.postValue(
            SubmissionNavigationEvents.NavigateToProfileList(
                ratProfileSettings.onboardedFlow.first()
            )
        )
    }

    fun onTestCenterPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.OpenTestCenterUrl)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDispatcherViewModel>
}
