package de.rki.coronawarnapp.ui.submission.qrcode.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.service.submission.QRScanResult
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.ScanStatus
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.Event
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionQRCodeScanViewModel @AssistedInject constructor() : CWAViewModel() {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()
    private val _scanStatus = MutableLiveData(Event(ScanStatus.STARTED))

    val scanStatus: LiveData<Event<ScanStatus>> = _scanStatus

    fun validateAndStoreTestGUID(rawResult: String) {
        val scanResult = QRScanResult(rawResult)
        if (scanResult.isValid) {
            SubmissionService.storeTestGUID(scanResult.guid!!)
            _scanStatus.value = Event(ScanStatus.SUCCESS)
        } else {
            _scanStatus.value = Event(ScanStatus.INVALID)
        }
    }

    val registrationState = MutableLiveData(ApiRequestState.IDLE)
    val registrationError = SingleLiveEvent<CwaWebException>()

    fun doDeviceRegistration() = launch {
        try {
            registrationState.postValue(ApiRequestState.STARTED)
            SubmissionService.asyncRegisterDevice()
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
        } catch (err: Exception) {
            registrationState.postValue(ApiRequestState.FAILED)
            err.report(ExceptionCategory.INTERNAL)
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
