package de.rki.coronawarnapp.ui.submission.qrcode.scan

import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.service.submission.QRScanResult
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.ScanStatus
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class SubmissionQRCodeScanViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository
) :
    CWAViewModel() {
    val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val scanStatusValue = SingleLiveEvent<ScanStatus>()

    open class InvalidQRCodeException : Exception("error in qr code")

    fun validateTestGUID(rawResult: String) {
        val scanResult = QRScanResult(rawResult)
        if (scanResult.isValid) {
            QRCodeCensor.lastGUID = scanResult.guid
            scanStatusValue.postValue(ScanStatus.SUCCESS)
            doDeviceRegistration(scanResult)
        } else {
            scanStatusValue.postValue(ScanStatus.INVALID)
        }
    }

    val registrationState = MutableLiveData(RegistrationState(ApiRequestState.IDLE))
    val registrationError = SingleLiveEvent<CwaWebException>()

    data class RegistrationState(
        val apiRequestState: ApiRequestState,
        val testResult: TestResult? = null
    )

    private fun doDeviceRegistration(scanResult: QRScanResult) = launch {
        try {
            registrationState.postValue(RegistrationState(ApiRequestState.STARTED))
            val testResult = submissionRepository.asyncRegisterDeviceViaGUID(scanResult.guid!!)
            checkTestResult(testResult)
            registrationState.postValue(RegistrationState(ApiRequestState.SUCCESS, testResult))
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
            deregisterTestFromDevice()
            showRedeemedTokenWarning.postValue(Unit)
        } catch (err: Exception) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    private fun checkTestResult(testResult: TestResult) {
        if (testResult == TestResult.REDEEMED) {
            throw InvalidQRCodeException()
        }
    }

    private fun deregisterTestFromDevice() {
        launch {
            Timber.d("deregisterTestFromDevice()")

            submissionRepository.removeTestFromDevice()

            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
        }
    }

    fun onBackPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToConsent)
    }

    fun onClosePressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDispatcher)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionQRCodeScanViewModel>
}
