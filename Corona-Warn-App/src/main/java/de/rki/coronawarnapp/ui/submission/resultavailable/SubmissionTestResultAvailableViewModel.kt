package de.rki.coronawarnapp.ui.submission.resultavailable

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.asLiveData
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SubmissionTestResultAvailableViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val tekHistoryUpdater: TEKHistoryUpdater,
    private val submissionRepository: SubmissionRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val clickEvent: SingleLiveEvent<SubmissionTestResultAvailableEvents> = SingleLiveEvent()

    val consent = submissionRepository.hasGivenConsentToSubmission.asLiveData(dispatcherProvider.Default)
    val showPermissionRequest = SingleLiveEvent<(Activity) -> Unit>()

    init {
        tekHistoryUpdater.callback = object : TEKHistoryUpdater.Callback {
            override fun onTEKAvailable(teks: List<TemporaryExposureKey>) {
                clickEvent.postValue(SubmissionTestResultAvailableEvents.GoToTestResult)
            }

            override fun onPermissionDeclined() {
                TODO("Not yet implemented")
            }

            override fun onError(error: Throwable) {
                Timber.e(error, "Failed to update TEKs.")
                error.report(
                    exceptionCategory = ExceptionCategory.EXPOSURENOTIFICATION,
                    prefix = "SubmissionTestResultAvailableViewModel"
                )
            }
        }
    }

    fun goBack() {
        clickEvent.postValue(SubmissionTestResultAvailableEvents.GoBack)
    }

    fun goConsent() {
        clickEvent.postValue(SubmissionTestResultAvailableEvents.GoConsent)
    }

    fun proceed() {
        launch {
            if (submissionRepository.hasGivenConsentToSubmission.first()) {
                tekHistoryUpdater.updateTEKHistoryOrRequestPermission { permissionRequest ->
                    showPermissionRequest.postValue(permissionRequest)
                }
            } else {
                clickEvent.postValue(SubmissionTestResultAvailableEvents.GoToTestResult)
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        tekHistoryUpdater.handleActivityResult(requestCode, resultCode, data)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultAvailableViewModel>
}
