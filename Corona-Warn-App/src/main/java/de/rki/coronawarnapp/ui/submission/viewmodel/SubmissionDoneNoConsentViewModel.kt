package de.rki.coronawarnapp.ui.submission.viewmodel

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionDoneNoConsentViewModel @AssistedInject constructor() : CWAViewModel() {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    fun onBackPressed() {
        // Flow needs to be validated once
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
    }

    fun onContinueWithSymptomRecordingPressed() {
        // Implementation will be done when other screens are ready
    }

    fun onBreakFlowPressed() {
        // Implementation will be done when other screens are ready
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDoneNoConsentViewModel>
}
