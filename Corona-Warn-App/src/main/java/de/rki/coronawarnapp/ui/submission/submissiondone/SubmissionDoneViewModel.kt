package de.rki.coronawarnapp.ui.submission.submissiondone

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionDoneViewModel @AssistedInject constructor() : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    fun onFinishButtonClick() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDoneViewModel>
}
