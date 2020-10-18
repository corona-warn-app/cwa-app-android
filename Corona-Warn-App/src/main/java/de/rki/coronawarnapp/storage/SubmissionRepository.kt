package de.rki.coronawarnapp.storage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date

object SubmissionRepository {
    private val TAG: String? = SubmissionRepository::class.simpleName

    val uiStateStateFlowInternal = MutableStateFlow(ApiRequestState.IDLE)
    val uiStateStateFlow: Flow<ApiRequestState> = uiStateStateFlowInternal
    val uiStateState: LiveData<ApiRequestState> = uiStateStateFlow.asLiveData()

    private val testResultReceivedDateFlowInternal = MutableStateFlow(Date())
    val testResultReceivedDateFlow: Flow<Date> = testResultReceivedDateFlowInternal
    val testResultReceivedDate = testResultReceivedDateFlow.asLiveData()

    private val deviceUIStateFlowInternal = MutableStateFlow(DeviceUIState.UNPAIRED)
    val deviceUIStateFlow: Flow<DeviceUIState> = deviceUIStateFlowInternal
    val deviceUIState = deviceUIStateFlow.asLiveData()

    private val testResult = MutableLiveData<TestResult?>(null)

    suspend fun refreshUIState(refreshTestResult: Boolean) {
        var uiState = DeviceUIState.UNPAIRED

        if (LocalData.numberOfSuccessfulSubmissions() == 1) {
            uiState = DeviceUIState.SUBMITTED_FINAL
        } else {
            if (LocalData.registrationToken() != null) {
                uiState = when {
                    LocalData.isAllowedToSubmitDiagnosisKeys() == true -> {
                        DeviceUIState.PAIRED_POSITIVE
                    }
                    refreshTestResult -> fetchTestResult()
                    else -> {
                        deriveUiState(testResult.value)
                    }
                }
            }
        }
        deviceUIStateFlowInternal.value = uiState
    }

    private suspend fun fetchTestResult(): DeviceUIState {
        try {
            val testResult = SubmissionService.asyncRequestTestResult()
            updateTestResult(testResult)
            return deriveUiState(testResult)
        } catch (err: NoRegistrationTokenSetException) {
            return DeviceUIState.UNPAIRED
        }
    }

    fun updateTestResult(testResult: TestResult) {
        this.testResult.value = testResult

        if (testResult == TestResult.POSITIVE) {
            LocalData.isAllowedToSubmitDiagnosisKeys(true)
        }

        val initialTestResultReceivedTimestamp = LocalData.initialTestResultReceivedTimestamp()

        if (initialTestResultReceivedTimestamp == null) {
            val currentTime = System.currentTimeMillis()
            LocalData.initialTestResultReceivedTimestamp(currentTime)
            testResultReceivedDateFlowInternal.value = Date(currentTime)
            if (testResult == TestResult.PENDING) {
                BackgroundWorkScheduler.startWorkScheduler()
            }
        } else {
            testResultReceivedDateFlowInternal.value = Date(initialTestResultReceivedTimestamp)
        }
    }

    private fun deriveUiState(testResult: TestResult?): DeviceUIState {
        return when (testResult) {
            TestResult.NEGATIVE -> DeviceUIState.PAIRED_NEGATIVE
            TestResult.POSITIVE -> DeviceUIState.PAIRED_POSITIVE
            TestResult.PENDING -> DeviceUIState.PAIRED_NO_RESULT
            TestResult.REDEEMED -> DeviceUIState.PAIRED_REDEEMED
            TestResult.INVALID -> DeviceUIState.PAIRED_ERROR
            null -> DeviceUIState.UNPAIRED
        }
    }

    fun setTeletan(teletan: String) {
        LocalData.teletan(teletan)
    }
}
