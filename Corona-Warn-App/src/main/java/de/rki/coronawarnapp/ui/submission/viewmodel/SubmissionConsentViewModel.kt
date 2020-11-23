package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull

class SubmissionConsentViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository,
    interoperabilityRepository: InteroperabilityRepository
) : CWAViewModel() {

    val countries = interoperabilityRepository.countryListFlow
        .filterNotNull().filter { it.isNotEmpty() }.asLiveData()

    fun onConsentButtonClick() {
        submissionRepository.giveConsentToSubmission()
    }
    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionConsentViewModel>
}
