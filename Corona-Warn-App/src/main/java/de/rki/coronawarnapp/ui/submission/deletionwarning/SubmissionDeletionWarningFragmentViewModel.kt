package de.rki.coronawarnapp.ui.submission.deletionwarning

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionDeletionWarningFragmentViewModel @AssistedInject constructor(
    private val coronaTestRepository: CoronaTestRepository,
) : CWAViewModel() {

    var testDeletionFinished = MutableLiveData<Boolean>()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDeletionWarningFragmentViewModel>

    fun deleteExistingTest(qrScanResult: CoronaTestQRCode) = launch {
        coronaTestRepository.removeTest(qrScanResult.type)
    }
}
