package de.rki.coronawarnapp.ui.submission.resultready

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class SubmissionResultReadyViewModel @AssistedInject constructor(
    private val autoSubmission: AutoSubmission,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val showUploadDialog = autoSubmission.isSubmissionRunning
        .asLiveData(context = dispatcherProvider.Default)
    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    fun onContinueWithSymptomRecordingPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSymptomIntroduction)
    }

    fun onSkipSymptomsConfirmed() {
        Timber.d("Symptom submission was skipped.")
        launch {
            try {
                autoSubmission.runSubmissionNow()
            } catch (e: Exception) {
                Timber.e(e, "greenlightSubmission() failed.")
            } finally {
                routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
            }
        }
    }

    fun onNewUserActivity() {
        Timber.d("onNewUserActivity()")
        autoSubmission.updateLastSubmissionUserActivity()
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionResultReadyViewModel>
}
