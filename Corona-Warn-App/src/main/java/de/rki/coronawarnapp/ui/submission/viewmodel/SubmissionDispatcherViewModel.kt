package de.rki.coronawarnapp.ui.submission.viewmodel


import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionDispatcherViewModel @AssistedInject constructor() : CWAViewModel() {

    companion object {
        private val TAG: String? = SubmissionDispatcherViewModel::class.simpleName
    }

    val navigateBack = SingleLiveEvent<Unit>()
    val navigateQRScan = SingleLiveEvent<Unit>()
    val navigateTAN = SingleLiveEvent<Unit>()
    val navigateTeleTAN = SingleLiveEvent<Unit>()

    fun onBackPressed() {
        navigateBack.postValue(Unit)
    }

    fun onQRScanPressed() {
        navigateQRScan.postValue(Unit)
    }

    fun onTanPressed() {
        navigateTAN.postValue(Unit)
    }

    fun onTeleTanPressed() {
        navigateTeleTAN.postValue(Unit)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDispatcherViewModel>
}
