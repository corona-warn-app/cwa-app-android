package de.rki.coronawarnapp.ui.submission.viewmodel

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionResultPositiveOtherWarningViewModel @AssistedInject constructor() : CWAViewModel() {

    val navigateBack = SingleLiveEvent<Unit>()
    val startWarningOthers = SingleLiveEvent<Unit>()
    val navigateSubmissionDone = SingleLiveEvent<Unit>()

    fun onBackPressed() {
        navigateBack.postValue(Unit)
    }

    fun onWarnOthersPressed() {
        startWarningOthers.postValue(Unit)
    }

    fun onSubmissionComplete() {
        navigateSubmissionDone.postValue(Unit)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionResultPositiveOtherWarningViewModel>
}
