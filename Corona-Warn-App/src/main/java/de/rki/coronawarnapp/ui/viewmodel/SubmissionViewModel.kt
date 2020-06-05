package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.report
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

    private val _registrationState = MutableLiveData(ApiRequestState.IDLE)
    private val _registrationError = MutableLiveData<Event<Exception>>(null)

    private val _uiStateState = MutableLiveData(ApiRequestState.IDLE)
    private val _uiStateError = MutableLiveData<Event<Exception>>(null)

    private val _submissionState = MutableLiveData(ApiRequestState.IDLE)
    private val _submissionError = MutableLiveData<Event<Exception>>(null)

    val scanStatus: LiveData<Event<ScanStatus>> = _scanStatus

    val registrationState: LiveData<ApiRequestState> = _registrationState
    val registrationError: LiveData<Event<Exception>> = _registrationError

    val uiStateState: LiveData<ApiRequestState> = _uiStateState
    val uiStateError: LiveData<Event<Exception>> = _uiStateError

    val submissionState: LiveData<ApiRequestState> = _submissionState
    val submissionError: LiveData<Event<Exception>> = _submissionError

    val deviceRegistered get() = LocalData.registrationToken() != null

    val testResultReceivedDate: LiveData<Date> =
        SubmissionRepository.testResultReceivedDate
    val deviceUiState: LiveData<DeviceUIState> =
        SubmissionRepository.deviceUIState

    fun submitDiagnosisKeys() =
        executeRequestWithState(
            SubmissionService::asyncSubmitExposureKeys,
            _submissionState,
            _submissionError
        )

    fun doDeviceRegistration() =
        executeRequestWithState(
            SubmissionService::asyncRegisterDevice,
            _registrationState,
            _registrationError
        )

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
        LocalData.inititalTestResultReceivedTimestamp(0L)
    }

    private fun executeRequestWithState(
        apiRequest: suspend () -> Unit,
        state: MutableLiveData<ApiRequestState>,
        exceptionLiveData: MutableLiveData<Event<Exception>>? = null
    ) {
        state.value = ApiRequestState.STARTED
        viewModelScope.launch {
            try {
                apiRequest()
                state.value = ApiRequestState.SUCCESS
            } catch (err: Exception) {
                exceptionLiveData?.value = Event(err)
                state.value = ApiRequestState.FAILED
            }
        }
    }
}
