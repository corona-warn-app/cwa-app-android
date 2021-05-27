package de.rki.coronawarnapp.ui.submission.qrcode.scan

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeRegistrationStateProcessor
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
    private val qrCodeRegistrationStateProcessor: QrCodeRegistrationStateProcessor,
    private val submissionRepository: SubmissionRepository,
    private val qrCodeValidator: CoronaTestQrCodeValidator,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
    val showRedeemedTokenWarning = qrCodeRegistrationStateProcessor.showRedeemedTokenWarning
    val qrCodeValidationState = SingleLiveEvent<QrCodeRegistrationStateProcessor.ValidationState>()
    val registrationState = qrCodeRegistrationStateProcessor.registrationState
    val registrationError = qrCodeRegistrationStateProcessor.registrationError

    fun onQrCodeAvailable(rawResult: String) {
        launch {
            startQrCodeRegistration(rawResult, isConsentGiven)
        }
    }

    suspend fun startQrCodeRegistration(rawResult: String, isConsentGiven: Boolean) {
        try {
            val coronaTestQRCode = qrCodeValidator.validate(rawResult)
            qrCodeValidationState.postValue(QrCodeRegistrationStateProcessor.ValidationState.SUCCESS)
            val coronaTest = submissionRepository.testForType(coronaTestQRCode.type).first()

            if (coronaTest != null) {
                routeToScreen.postValue(
                    SubmissionNavigationEvents.NavigateToDeletionWarningFragmentFromQrCode(
                        coronaTestQRCode = coronaTestQRCode,
                        consentGiven = isConsentGiven
                    )
                )
            } else {
                if (isConsentGiven) {
                    analyticsKeySubmissionCollector.reportAdvancedConsentGiven(coronaTestQRCode.type)
                }
                qrCodeRegistrationStateProcessor.startQrCodeRegistration(coronaTestQRCode, isConsentGiven)
            }
        } catch (err: InvalidQRCodeException) {
            qrCodeValidationState.postValue(QrCodeRegistrationStateProcessor.ValidationState.INVALID)
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
    interface Factory : CWAViewModelFactory<SubmissionQRCodeScanViewModel> {
        fun create(isConsentGiven: Boolean): SubmissionQRCodeScanViewModel
    }
}
