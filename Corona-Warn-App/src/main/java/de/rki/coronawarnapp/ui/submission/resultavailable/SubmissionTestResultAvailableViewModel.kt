package de.rki.coronawarnapp.ui.submission.resultavailable

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionTestResultAvailableViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    submissionRepository: SubmissionRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val clickEvent: SingleLiveEvent<SubmissionTestResultAvailableEvents> = SingleLiveEvent()

    val consent = submissionRepository.hasGivenConsentToSubmission.asLiveData(dispatcherProvider.Default)

    fun goBack() {
        clickEvent.postValue(SubmissionTestResultAvailableEvents.GoBack)
    }

    fun goConsent() {
        clickEvent.postValue(SubmissionTestResultAvailableEvents.GoConsent)
    }

    fun proceed() {
        clickEvent.postValue(SubmissionTestResultAvailableEvents.Proceed)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultAvailableViewModel>
}
