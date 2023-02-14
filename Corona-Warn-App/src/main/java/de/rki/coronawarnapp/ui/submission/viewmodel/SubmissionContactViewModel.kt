package de.rki.coronawarnapp.ui.submission.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

@HiltViewModel
class SubmissionContactViewModel @Inject constructor() : CWAViewModel() {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    fun onBackPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDispatcher)
    }

    fun onEnterTanPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToTAN)
    }
}
