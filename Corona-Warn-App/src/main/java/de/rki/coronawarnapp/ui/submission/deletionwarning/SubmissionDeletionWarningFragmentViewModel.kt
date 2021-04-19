package de.rki.coronawarnapp.ui.submission.deletionwarning

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionDeletionWarningFragmentViewModel @AssistedInject constructor(
    private val coronaTestRepository: CoronaTestRepository,
) : CWAViewModel() {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDeletionWarningFragmentViewModel>

    fun deleteExistingAndRegisterNewTest(qrScanResult: CoronaTestQRCode) = launch {
        coronaTestRepository.removeTest(qrScanResult.type)

        // TODO Register test
    }
}
