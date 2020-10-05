package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.util.ui.SingleLiveEvent

class SubmissionQRCodeInfoFragmentViewModel : ViewModel() {

    val navigateBack = SingleLiveEvent<Boolean>()
    val navigateForward = SingleLiveEvent<Boolean>()

    fun onBackPressed() {
        navigateBack.postValue(true)
    }

    fun onNextPressed() {
        navigateForward.postValue(true)
    }
}
