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
import kotlinx.coroutines.launch
import java.util.Date

class SubmissionViewModel : ViewModel() {
    private val _scanStatus = MutableLiveData(ScanStatus.STARTED)
    private val _registrationState = MutableLiveData(ApiRequestState.IDLE)
    private val _uiStateState = MutableLiveData(ApiRequestState.IDLE)
    private val _submissionState = MutableLiveData(ApiRequestState.IDLE)

    val scanStatus: LiveData<ScanStatus> = _scanStatus
    val registrationState: LiveData<ApiRequestState> = _registrationState
    val uiStateState: LiveData<ApiRequestState> = _uiStateState
    val submissionState: LiveData<ApiRequestState> = _submissionState

    val deviceRegistered get() = LocalData.registrationToken() != null

    val testResultReceivedDate: LiveData<Date> =
        SubmissionRepository.testResultReceivedDate
    val deviceUiState: LiveData<DeviceUIState> =
        SubmissionRepository.deviceUIState

    fun submitDiagnosisKeys() =
        executeRequestWithState(SubmissionService::asyncSubmitExposureKeys, _submissionState)

    fun doDeviceRegistration() =
        executeRequestWithState(SubmissionService::asyncRegisterDevice, _registrationState)

    fun refreshDeviceUIState() =
        executeRequestWithState(SubmissionRepository::refreshUIState, _uiStateState)

    fun validateAndStoreTestGUID(scanResult: String) {
        if (SubmissionService.containsValidGUID(scanResult)) {
            val guid = SubmissionService.extractGUID(scanResult)
            SubmissionService.storeTestGUID(guid)
            _scanStatus.value = ScanStatus.SUCCESS
        } else {
            _scanStatus.value = ScanStatus.INVALID
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
        state: MutableLiveData<ApiRequestState>
    ) {
        state.value = ApiRequestState.STARTED
        viewModelScope.launch {
            try {
                apiRequest()
                state.value = ApiRequestState.SUCCESS
            } catch (err: Exception) {
                state.value = ApiRequestState.FAILED
                err.report(ExceptionCategory.INTERNAL)
            }
        }
    }
}
