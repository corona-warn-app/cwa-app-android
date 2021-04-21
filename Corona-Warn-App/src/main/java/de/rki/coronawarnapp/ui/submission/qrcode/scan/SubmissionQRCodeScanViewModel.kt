package de.rki.coronawarnapp.ui.submission.qrcode.scan

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeSubmission
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
    private val cameraSettings: CameraSettings,
    private val qrCodeSubmission: QrCodeSubmission,
    @Assisted private val isConsentGiven: Boolean,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
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

    fun triggerNavigationToSubmissionTestResultAvailableFragment() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToResultAvailableScreen)
    }

    fun triggerNavigationToSubmissionTestResultPendingFragment() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToResultPendingScreen)
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


// todo
    fun validateTestGUID(rawResult: String) = launch {
        try {
            val coronaTestQRCode = qrCodeValidator.validate(rawResult)
            // TODO this needs to be adapted to work for different types
            QRCodeCensor.lastGUID = coronaTestQRCode.registrationIdentifier
            scanStatusValue.postValue(ScanStatus.SUCCESS)

            val coronaTest = submissionRepository.testForType(coronaTestQRCode.type).first()

            if (coronaTest != null) {
                routeToScreen.postValue(
                    SubmissionNavigationEvents.NavigateToDeletionWarningFragment(
                        coronaTestQRCode,
                        isConsentGiven
                    )
                )
            } else {
                doDeviceRegistration(coronaTestQRCode)
            }
        } catch (err: InvalidQRCodeException) {
            Timber.e(err, "Failed to validate GUID")
            scanStatusValue.postValue(ScanStatus.INVALID)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun doDeviceRegistration(coronaTestQRCode: CoronaTestQRCode) {
        try {
            registrationState.postValue(RegistrationState(ApiRequestState.STARTED))
            val coronaTest = submissionRepository.registerTest(coronaTestQRCode)
            if (isConsentGiven) {
                submissionRepository.giveConsentToSubmission(type = coronaTestQRCode.type)
            }
            checkTestResult(coronaTest.testResult)
            registrationState.postValue(RegistrationState(ApiRequestState.SUCCESS, coronaTest.testResult))
        } catch (err: CwaWebException) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            registrationError.postValue(err)
        } catch (err: TransactionException) {
            if (err.cause is CwaWebException) {
                registrationError.postValue(err.cause)
            } else {
                err.report(ExceptionCategory.INTERNAL)
            }
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
        } catch (err: InvalidQRCodeException) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            deregisterTestFromDevice(coronaTestQRCode)
            showRedeemedTokenWarning.postValue(Unit)
        } catch (err: Exception) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    private fun deregisterTestFromDevice(coronaTest: CoronaTestQRCode) {
        launch {
            Timber.d("deregisterTestFromDevice()")

            submissionRepository.removeTestFromDevice(type = coronaTest.type)
            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
        }
    }

    private fun checkTestResult(testResult: CoronaTestResult) {
        if (testResult == CoronaTestResult.PCR_REDEEMED) {
            throw InvalidQRCodeException()
        }
    }
}
