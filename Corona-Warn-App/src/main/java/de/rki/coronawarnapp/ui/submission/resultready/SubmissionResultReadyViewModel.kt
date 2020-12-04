package de.rki.coronawarnapp.ui.submission.resultready

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class SubmissionResultReadyViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val showUploadDialog = submissionRepository.isSubmissionRunning
        .asLiveData(context = dispatcherProvider.Default)
    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    fun onContinueWithSymptomRecordingPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSymptomIntroduction)
    }

    fun onSkipSymptomInput() {
        Timber.d("Symptom submission was cancelled.")
        launch {
            try {
                submissionRepository.startSubmission()
            } catch (e: Exception) {
                Timber.e(e, "onCancelConfirmed() failed.")
            } finally {
                routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
            }
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionResultReadyViewModel>
}
