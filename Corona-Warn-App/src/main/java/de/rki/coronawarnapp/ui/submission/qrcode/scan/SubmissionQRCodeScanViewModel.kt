package de.rki.coronawarnapp.ui.submission.qrcode.scan

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SubmissionQRCodeScanViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val isConsentGiven: Boolean,
    private val cameraSettings: CameraSettings,
    private val registrationStateProcessor: TestRegistrationStateProcessor,
    private val submissionRepository: SubmissionRepository,
    private val qrCodeValidator: CoronaTestQrCodeValidator,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<SubmissionNavigationEvents>()
    val qrCodeErrorEvent = SingleLiveEvent<Exception>()
    val registrationState = registrationStateProcessor.state.asLiveData2()

    fun registerCoronaTest(rawResult: String) = launch {
        try {
            val ctQrCode = qrCodeValidator.validate(rawResult)

            val coronaTest = submissionRepository.testForType(ctQrCode.type).first()
            when {
                coronaTest != null -> events.postValue(
                    SubmissionNavigationEvents.NavigateToDeletionWarningFragmentFromQrCode(
                        coronaTestQRCode = ctQrCode,
                        consentGiven = isConsentGiven
                    )
                )

                else -> if (!ctQrCode.isDccSupportedByPoc) {
                    registrationStateProcessor.startRegistration(
                        request = ctQrCode,
                        isSubmissionConsentGiven = isConsentGiven,
                        allowReplacement = false
                    )
                } else {
                    events.postValue(
                        SubmissionNavigationEvents.NavigateToRequestDccFragment(ctQrCode, isConsentGiven)
                    )
                }
            }
        } catch (err: InvalidQRCodeException) {
            Timber.d(err, "Invalid QrCode")
            qrCodeErrorEvent.postValue(err)
        }
    }

    fun onBackPressed() = events.postValue(SubmissionNavigationEvents.NavigateToConsent)

    fun onClosePressed() = events.postValue(SubmissionNavigationEvents.NavigateToDispatcher)

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionQRCodeScanViewModel> {
        fun create(isConsentGiven: Boolean): SubmissionQRCodeScanViewModel
    }
}
