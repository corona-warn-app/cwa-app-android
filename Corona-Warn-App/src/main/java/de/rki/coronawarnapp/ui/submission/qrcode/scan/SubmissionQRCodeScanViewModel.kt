package de.rki.coronawarnapp.ui.submission.qrcode.scan

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class SubmissionQRCodeScanViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val cameraSettings: CameraSettings,
    private val qrCodeValidator: CoronaTestQrCodeValidator,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<SubmissionNavigationEvents>()
    val qrCodeErrorEvent = SingleLiveEvent<Exception>()

    fun registerCoronaTest(rawResult: String) = launch {
        try {
            qrCodeValidator.validate(rawResult)
            events.postValue(SubmissionNavigationEvents.NavigateToConsent(qrCode = rawResult))
        } catch (err: InvalidQRCodeException) {
            Timber.d(err, "Invalid QrCode")
            qrCodeErrorEvent.postValue(err)
        }
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionQRCodeScanViewModel>
}
