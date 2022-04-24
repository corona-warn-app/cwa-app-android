package de.rki.coronawarnapp.ui.submission.submissiondone

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory

class SubmissionDoneViewModel @AssistedInject constructor(
    @Assisted val testType: BaseCoronaTest.Type

) : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    fun onFinishButtonClick() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionDoneViewModel> {
        fun create(testType: BaseCoronaTest.Type): SubmissionDoneViewModel
    }
}
