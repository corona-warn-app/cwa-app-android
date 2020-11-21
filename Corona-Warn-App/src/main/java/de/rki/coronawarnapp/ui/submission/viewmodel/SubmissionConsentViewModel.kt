package de.rki.coronawarnapp.ui.submission.viewmodel

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionConsentViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository
) : CWAViewModel() {

    fun onConsentButtonClick() {
        submissionRepository.giveConsentToSubmission()
    }
    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionConsentViewModel>
}
