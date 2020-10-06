package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.util.ui.SingleLiveEvent

class SubmissionQRCodeInfoFragmentViewModel : ViewModel() {

    val navigateToDispatcher = SingleLiveEvent<Unit>()
    val navigateToQRScan = SingleLiveEvent<Unit>()

    fun onBackPressed() {
        navigateToDispatcher.postValue(Unit)
    }

    fun onNextPressed() {
        navigateToQRScan.postValue(Unit)
    }
}
