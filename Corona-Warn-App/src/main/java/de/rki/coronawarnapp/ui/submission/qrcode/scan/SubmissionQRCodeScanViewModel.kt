package de.rki.coronawarnapp.ui.submission.qrcode.scan

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.ScanStatus
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
    private val submissionRepository: SubmissionRepository,
    private val cameraSettings: CameraSettings,
    @Assisted private val isConsentGiven: Boolean,
    private val qrCodeValidator: CoronaTestQrCodeValidator
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val scanStatusValue = SingleLiveEvent<ScanStatus>()

    fun validateTestGUID(rawResult: String) = launch {
        Timber.d("validateTestGUID(rawResult=$rawResult)")
        try {
            val coronaTestQRCode = qrCodeValidator.validate(rawResult)
            Timber.d("validateTestGUID() coronaTestQRCode=%s", coronaTestQRCode)
            // TODO this needs to be adapted to work for different types
            QRCodeCensor.lastGUID = coronaTestQRCode.registrationIdentifier
            scanStatusValue.postValue(ScanStatus.SUCCESS)

            val coronaTest = submissionRepository.testForType(coronaTestQRCode.type).first()
            Timber.d("validateTestGUID() existingTest=%s", coronaTest)

            if (coronaTest != null) {
                routeToScreen.postValue(
                    SubmissionNavigationEvents.NavigateToDeletionWarningFragmentFromQrCode(
                        coronaTestQRCode = coronaTestQRCode,
                        consentGiven = isConsentGiven
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

    val registrationState = MutableLiveData(RegistrationState(ApiRequestState.IDLE))
    val registrationError = SingleLiveEvent<CwaWebException>()

    data class RegistrationState(
        val apiRequestState: ApiRequestState,
        val testResult: CoronaTestResult? = null,
        val testType: CoronaTest.Type? = null
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun doDeviceRegistration(coronaTestQRCode: CoronaTestQRCode) {
        try {
            Timber.d("doDeviceRegistration(coronaTestQRCode=%s)", coronaTestQRCode)
            registrationState.postValue(RegistrationState(ApiRequestState.STARTED))
            val coronaTest = submissionRepository.registerTest(coronaTestQRCode)
            Timber.d("doDeviceRegistration() coronaTest=$coronaTest")
            if (isConsentGiven) {
                submissionRepository.giveConsentToSubmission(type = coronaTestQRCode.type)
            }
            checkTestResult(coronaTestQRCode, coronaTest)
            registrationState.postValue(
                RegistrationState(
                    ApiRequestState.SUCCESS,
                    coronaTest.testResult,
                    coronaTestQRCode.type
                )
            )
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

    private fun checkTestResult(request: CoronaTestQRCode, test: CoronaTest) {
        if (test.testResult == CoronaTestResult.PCR_REDEEMED) {
            throw InvalidQRCodeException("CoronaTestResult already redeemed ${request.registrationIdentifier}")
        }
    }

    private fun deregisterTestFromDevice(coronaTest: CoronaTestQRCode) {
        launch {
            Timber.d("deregisterTestFromDevice(coronaTest=%s)", coronaTest)
            submissionRepository.removeTestFromDevice(type = coronaTest.type)
            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
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
