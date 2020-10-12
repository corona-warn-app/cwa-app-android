package de.rki.coronawarnapp.ui.submission.viewmodel

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionIntroViewModel @AssistedInject constructor() : CWAViewModel() {

    val navigateBack = SingleLiveEvent<Unit>()
    val navigateToDispatcher = SingleLiveEvent<Unit>()

    fun onBackPressed() {
        navigateBack.postValue(Unit)
    }

    fun onNextPressed() {
        navigateToDispatcher.postValue(Unit)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionIntroViewModel>
}
