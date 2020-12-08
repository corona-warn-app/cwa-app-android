package de.rki.coronawarnapp.ui.submission.resultavailable

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
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

    val routeToScreen = SingleLiveEvent<NavDirections>()

    val consentFlow = submissionRepository.hasGivenConsentToSubmission
    val consent = consentFlow.asLiveData(dispatcherProvider.Default)
    val showPermissionRequest = SingleLiveEvent<(Activity) -> Unit>()
    val showCloseDialog = SingleLiveEvent<Unit>()

    init {
        submissionRepository.refreshDeviceUIState(refreshTestResult = false)

        tekHistoryUpdater.callback = object : TEKHistoryUpdater.Callback {
            override fun onTEKAvailable(teks: List<TemporaryExposureKey>) {
                routeToScreen.postValue(
                    SubmissionTestResultAvailableFragmentDirections
                        .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultConsentGivenFragment()
                )
            }

            override fun onPermissionDeclined() {
                routeToScreen.postValue(
                    SubmissionTestResultAvailableFragmentDirections
                        .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultNoConsentFragment()
                )
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
        showCloseDialog.postValue(Unit)
    }

    fun onCancelConfirmed() {
        routeToScreen.postValue(
            SubmissionTestResultAvailableFragmentDirections
                .actionSubmissionTestResultAvailableFragmentToMainFragment()
        )
    }

    fun goConsent() {
        routeToScreen.postValue(
            SubmissionTestResultAvailableFragmentDirections
                .actionSubmissionTestResultAvailableFragmentToSubmissionYourConsentFragment(
                    isTestResultAvailable = true
                )
        )
    }

    fun proceed() {
        launch {
            if (consentFlow.first()) {
                tekHistoryUpdater.updateTEKHistoryOrRequestPermission { permissionRequest ->
                    showPermissionRequest.postValue(permissionRequest)
                }
            } else {
                routeToScreen.postValue(
                    SubmissionTestResultAvailableFragmentDirections
                        .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultNoConsentFragment()
                )
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        tekHistoryUpdater.handleActivityResult(requestCode, resultCode, data)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultAvailableViewModel>
}
