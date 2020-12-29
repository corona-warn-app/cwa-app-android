package de.rki.coronawarnapp.ui.submission.yourconsent

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first

class SubmissionYourConsentViewModel @AssistedInject constructor(
    val dispatcherProvider: DispatcherProvider,
    interoperabilityRepository: InteroperabilityRepository,
    val submissionRepository: SubmissionRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val clickEvent: SingleLiveEvent<SubmissionYourConsentEvents> = SingleLiveEvent()
    val consent = submissionRepository.hasGivenConsentToSubmission.asLiveData()
    val countryList = interoperabilityRepository.countryList
        .asLiveData(context = dispatcherProvider.Default)

    fun goBack() {
        clickEvent.postValue(SubmissionYourConsentEvents.GoBack)
    }

    fun switchConsent() {
        launch {
            if (submissionRepository.hasGivenConsentToSubmission.first()) {
                submissionRepository.revokeConsentToSubmission()
            } else {
                submissionRepository.giveConsentToSubmission()
            }
        }
    }

    fun goLegal() {
        clickEvent.postValue(SubmissionYourConsentEvents.GoLegal)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionYourConsentViewModel>
}
