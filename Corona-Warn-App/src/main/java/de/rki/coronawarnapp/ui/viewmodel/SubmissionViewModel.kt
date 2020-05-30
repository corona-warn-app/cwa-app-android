package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.InvalidQRCodeExcpetion
import de.rki.coronawarnapp.exception.report
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.register.ApiRequestState
import de.rki.coronawarnapp.ui.register.ScanStatus
import de.rki.coronawarnapp.util.formatter.TestResult
import kotlinx.coroutines.launch
import java.util.Date

class SubmissionViewModel : ViewModel() {
    private val _scanStatus = MutableLiveData(ScanStatus.STARTED)
    private val _registrationState = MutableLiveData(ApiRequestState.IDLE)
    private val _testResultState = MutableLiveData(ApiRequestState.IDLE)
    private val _authCodeState = MutableLiveData(ApiRequestState.IDLE)
    private val _submissionState = MutableLiveData(ApiRequestState.IDLE)

    val scanStatus: LiveData<ScanStatus> = _scanStatus
    val registrationState: LiveData<ApiRequestState> = _registrationState
    val testResultState: LiveData<ApiRequestState> = _testResultState
    val authCodeState: LiveData<ApiRequestState> = _authCodeState
    val submissionState: LiveData<ApiRequestState> = _submissionState

    val deviceRegistered get() = LocalData.registrationToken() != null

    val testResult: LiveData<TestResult> =
        SubmissionRepository.testResult
    val testResultReceivedDate: LiveData<Date> = SubmissionRepository.testResultReceivedDate

    fun submitDiagnosisKeys() =
        executeRequestWithState(SubmissionService::asyncSubmitExposureKeys, _submissionState)

    fun doDeviceRegistration() =
        executeRequestWithState(SubmissionService::asyncRegisterDevice, _registrationState)

    fun refreshTestResult() =
        executeRequestWithState(SubmissionRepository::refreshTestResult, _testResultState)

    fun validateAndStoreTestGUID(testGUID: String) {
        try {
            SubmissionService.validateAndStoreTestGUID(testGUID)
            _scanStatus.value = ScanStatus.SUCCESS
        } catch (ex: InvalidQRCodeExcpetion) {
            _scanStatus.value = ScanStatus.INVALID
        }
    }

    fun deleteTestGUID() {
        SubmissionService.deleteTestGUID()
    }

    fun deregisterTestFromDevice() {
        deleteTestGUID()
        SubmissionService.deleteRegistrationToken()
    }

    private fun executeRequestWithState(apiRequest: suspend () -> Unit, state: MutableLiveData<ApiRequestState>) {
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
