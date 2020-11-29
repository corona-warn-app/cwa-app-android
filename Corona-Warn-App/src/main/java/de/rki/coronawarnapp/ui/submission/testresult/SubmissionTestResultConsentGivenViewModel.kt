package de.rki.coronawarnapp.ui.submission.testresult

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class SubmissionTestResultConsentGivenViewModel @AssistedInject constructor() : CWAViewModel() {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val showCancelDialog = SingleLiveEvent<Unit>()

    fun onContinuePressed(){
        Timber.d("Beginning symptom flow")
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSymptomIntroduction)
    }

    fun onShowCancelDialog() {
        showCancelDialog.postValue(Unit)
    }

    fun cancelTestSubmission() {
        Timber.d("Submission was cancelled.")
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
    }


    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultConsentGivenViewModel>
}
