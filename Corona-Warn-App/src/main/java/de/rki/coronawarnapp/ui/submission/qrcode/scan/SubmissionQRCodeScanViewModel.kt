package de.rki.coronawarnapp.ui.submission.qrcode.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedIOException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.service.submission.QRScanResult
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.ScanStatus
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.Event
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class SubmissionQRCodeScanViewModel @AssistedInject constructor() :
    CWAViewModel() {
    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()
    private val _scanStatus = MutableLiveData(Event(ScanStatus.STARTED))

    val scanStatus: LiveData<Event<ScanStatus>> = _scanStatus
    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()

    open class InvalidQRCodeException : ReportedIOException(
        ErrorCodes.CWA_WEB_REQUEST_PROBLEM.code, "error in qr code"
    )

    fun validateTestGUID(rawResult: String) {
        val scanResult = QRScanResult(rawResult)
        if (scanResult.isValid) {
            _scanStatus.value = Event(ScanStatus.SUCCESS)
            doDeviceRegistration(scanResult)
        } else {
            _scanStatus.value = Event(ScanStatus.INVALID)
        }
    }

    val registrationState = MutableLiveData(ApiRequestState.IDLE)
    val registrationError = SingleLiveEvent<CwaWebException>()

    private fun doDeviceRegistration(scanResult: QRScanResult) = launch {
        try {
            registrationState.postValue(ApiRequestState.STARTED)
            checkTestResult(SubmissionService.asyncRegisterDeviceViaGUID(scanResult.guid!!))
            registrationState.postValue(ApiRequestState.SUCCESS)
        } catch (err: CwaWebException) {
            registrationState.postValue(ApiRequestState.FAILED)
            registrationError.postValue(err)
        } catch (err: TransactionException) {
            if (err.cause is CwaWebException) {
                registrationError.postValue(err.cause)
            } else {
                err.report(ExceptionCategory.INTERNAL)
            }
            registrationState.postValue(ApiRequestState.FAILED)
        } catch (err: InvalidQRCodeException) {
            registrationState.postValue(ApiRequestState.FAILED)
            showRedeemedTokenWarning.postValue(Unit)
        } catch (err: Exception) {
            registrationState.postValue(ApiRequestState.FAILED)
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    private fun checkTestResult(testResult: TestResult) {
        if (testResult == TestResult.REDEEMED) {
            throw InvalidQRCodeException()
        }
    }

    fun deregisterTestFromDevice() {
        launch {
            Timber.d("deregisterTestFromDevice()")
            SubmissionService.deleteTestGUID()
            SubmissionService.deleteRegistrationToken()
            LocalData.isAllowedToSubmitDiagnosisKeys(false)
            LocalData.initialTestResultReceivedTimestamp(0L)

            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
        }
    }

    fun onBackPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToQRInfo)
    }

    fun onClosePressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDispatcher)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionQRCodeScanViewModel>
}
