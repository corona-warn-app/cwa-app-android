package de.rki.coronawarnapp.ui.submission.qrcode.scan

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeSubmission
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class SubmissionQRCodeScanViewModel @AssistedInject constructor(
    private val cameraSettings: CameraSettings,
    private val qrCodeSubmission: QrCodeSubmission
) : CWAViewModel() {

    val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
    val showRedeemedTokenWarning = qrCodeSubmission.showRedeemedTokenWarning
    val qrCodeValidationState = qrCodeSubmission.qrCodeValidationState
    val registrationState = qrCodeSubmission.registrationState
    val registrationError = qrCodeSubmission.registrationError

    fun onQrCodeAvailable(rawResult: String) {
        launch {
            qrCodeSubmission.startQrCodeRegistration(rawResult)
        }
    }

    fun onBackPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToConsent)
    }

    fun onClosePressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDispatcher)
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionQRCodeScanViewModel>
}
