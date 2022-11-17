package de.rki.coronawarnapp.ui.submission.viewmodel

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.profile.storage.ProfileSettingsDataStore
import de.rki.coronawarnapp.srs.core.SrsLocalChecker
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SubmissionDispatcherViewModel @AssistedInject constructor(
    private val profileSettings: ProfileSettingsDataStore,
    private val srsLocalChecker: SrsLocalChecker,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

    val srsError = SingleLiveEvent<SrsSubmissionException>()

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

    fun onProfilePressed() = launch {
        routeToScreen.postValue(
            SubmissionNavigationEvents.NavigateToProfileList(
                profileSettings.onboardedFlow.first()
            )
        )
    }

    fun onSrsTileClicked(
        positiveNoAnswer: Boolean = false
    ) = launch {
        try {
            srsLocalChecker.check()
            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSelfTestConsentScreen(positiveNoAnswer))
        } catch (e: SrsSubmissionException) {
            srsError.postValue(e)
            Timber.d(e, "onPositiveTestWithNoResult()")
        }
    }

    fun onTestCenterPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.OpenTestCenterUrl)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDispatcherViewModel>
}
