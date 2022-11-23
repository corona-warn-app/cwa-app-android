package de.rki.coronawarnapp.ui.submission.resultready

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import timber.log.Timber

class SubmissionResultReadyViewModel @AssistedInject constructor(
    private val autoSubmission: AutoSubmission,
    dispatcherProvider: DispatcherProvider,
    @Assisted val testType: BaseCoronaTest.Type,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val mediatorShowUploadDialog = MediatorLiveData<Boolean>()

    init {
        mediatorShowUploadDialog.addSource(
            autoSubmission.isSubmissionRunning.asLiveData(context = dispatcherProvider.Default)
        ) { show ->
            mediatorShowUploadDialog.postValue(show)
        }
    }

    val showUploadDialog: LiveData<Boolean> = mediatorShowUploadDialog

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    fun onContinueWithSymptomRecordingPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSymptomIntroduction)
    }

    fun onSkipSymptomsConfirmed() {
        Timber.d("Symptom submission was skipped.")
        launch {
            try {
                autoSubmission.runSubmissionNow(testType)
            } catch (e: Exception) {
                Timber.e(e, "greenlightSubmission() failed.")
            } finally {
                Timber.i("Hide uploading progress and navigate to MainActivity")
                mediatorShowUploadDialog.postValue(false)
                routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
            }
        }
    }

    fun onNewUserActivity() = launch {
        autoSubmission.updateLastSubmissionUserActivity().also {
            Timber.d("onNewUserActivity()")
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionResultReadyViewModel> {
        fun create(testType: BaseCoronaTest.Type): SubmissionResultReadyViewModel
    }
}
