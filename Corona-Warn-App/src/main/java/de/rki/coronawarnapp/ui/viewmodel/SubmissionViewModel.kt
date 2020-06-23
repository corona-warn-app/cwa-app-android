package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.ScanStatus
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.Event
import kotlinx.coroutines.launch
import java.util.Date

class SubmissionViewModel : ViewModel() {
    private val _scanStatus = MutableLiveData(Event(ScanStatus.STARTED))

    private val _registrationState = MutableLiveData(Event(ApiRequestState.IDLE))
    private val _registrationError = MutableLiveData<Event<CwaWebException>>(null)

    private val _uiStateState = MutableLiveData(ApiRequestState.IDLE)
    private val _uiStateError = MutableLiveData<Event<CwaWebException>>(null)

    private val _submissionState = MutableLiveData(ApiRequestState.IDLE)
    private val _submissionError = MutableLiveData<Event<CwaWebException>>(null)

    val scanStatus: LiveData<Event<ScanStatus>> = _scanStatus

    val registrationState: LiveData<Event<ApiRequestState>> = _registrationState
    val registrationError: LiveData<Event<CwaWebException>> = _registrationError

    val uiStateState: LiveData<ApiRequestState> = _uiStateState
    val uiStateError: LiveData<Event<CwaWebException>> = _uiStateError

    val submissionState: LiveData<ApiRequestState> = _submissionState
    val submissionError: LiveData<Event<CwaWebException>> = _submissionError

    val deviceRegistered get() = LocalData.registrationToken() != null

    val testResultReceivedDate: LiveData<Date> =
        SubmissionRepository.testResultReceivedDate
    val deviceUiState: LiveData<DeviceUIState> =
        SubmissionRepository.deviceUIState

    fun submitDiagnosisKeys(keys: List<TemporaryExposureKey>) = viewModelScope.launch {
        try {
            _submissionState.value = ApiRequestState.STARTED
            SubmissionService.asyncSubmitExposureKeys(keys)
            _submissionState.value = ApiRequestState.SUCCESS
        } catch (err: CwaWebException) {
            _submissionError.value = Event(err)
            _submissionState.value = ApiRequestState.FAILED
        } catch (err: TransactionException) {
            if (err.cause is CwaWebException) {
                _submissionError.value = Event(err.cause)
            } else {
                err.report(ExceptionCategory.INTERNAL)
            }
            _submissionState.value = ApiRequestState.FAILED
        } catch (err: Exception) {
            _submissionState.value = ApiRequestState.FAILED
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    fun doDeviceRegistration() = viewModelScope.launch {
        try {
            _registrationState.value = Event(ApiRequestState.STARTED)
            SubmissionService.asyncRegisterDevice()
            _registrationState.value = Event(ApiRequestState.SUCCESS)
        } catch (err: CwaWebException) {
            _registrationError.value = Event(err)
            _registrationState.value = Event(ApiRequestState.FAILED)
        } catch (err: TransactionException) {
            if (err.cause is CwaWebException) {
                _registrationError.value = Event(err.cause)
            } else {
                err.report(ExceptionCategory.INTERNAL)
            }
            _registrationState.value = Event(ApiRequestState.FAILED)
        } catch (err: Exception) {
            _registrationState.value = Event(ApiRequestState.FAILED)
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    fun refreshDeviceUIState() =
        executeRequestWithState(
            SubmissionRepository::refreshUIState,
            _uiStateState,
            _uiStateError
        )

    fun validateAndStoreTestGUID(scanResult: String) {
        if (SubmissionService.containsValidGUID(scanResult)) {
            val guid = SubmissionService.extractGUID(scanResult)
            SubmissionService.storeTestGUID(guid)
            _scanStatus.value = Event(ScanStatus.SUCCESS)
        } else {
            _scanStatus.value = Event(ScanStatus.INVALID)
        }
    }

    fun deleteTestGUID() {
        SubmissionService.deleteTestGUID()
    }

    fun deregisterTestFromDevice() {
        deleteTestGUID()
        SubmissionService.deleteRegistrationToken()
        LocalData.isAllowedToSubmitDiagnosisKeys(false)
        LocalData.initialTestResultReceivedTimestamp(0L)
    }

    private fun executeRequestWithState(
        apiRequest: suspend () -> Unit,
        state: MutableLiveData<ApiRequestState>,
        exceptionLiveData: MutableLiveData<Event<CwaWebException>>? = null
    ) {
        state.value = ApiRequestState.STARTED
        viewModelScope.launch {
            try {
                apiRequest()
                state.value = ApiRequestState.SUCCESS
            } catch (err: CwaWebException) {
                exceptionLiveData?.value = Event(err)
                state.value = ApiRequestState.FAILED
            } catch (err: Exception) {
                err.report(ExceptionCategory.INTERNAL)
            }
        }
    }
}
