package de.rki.coronawarnapp.ui.submission.deletionwarning

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionDeletionWarningFragmentViewModel @AssistedInject constructor() : CWAViewModel() {
    //val routeToScreen: SingleLiveEvent<SubmissionRemovePriorTestNavigationEvents> = SingleLiveEvent()

    /*
    fun onNextButtonClick() {
        routeToScreen.postValue(OverwriteInformationNavigationEvents.NavigateToOverviewFragment)
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(OverwriteInformationNavigationEvents.NavigateToMainActivity)
    }

    fun onPrivacyButtonPress() {
        routeToScreen.postValue(OverwriteInformationNavigationEvents.NavigateToPrivacyFragment)
    }*/

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDeletionWarningFragmentViewModel>
}
