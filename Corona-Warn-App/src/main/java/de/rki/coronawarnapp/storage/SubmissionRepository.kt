package de.rki.coronawarnapp.storage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.Event
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date

object SubmissionRepository {

    private val appScope by lazy {
        AppInjector.component.appScope
    }

    val uiStateStateFlowInternal = MutableStateFlow(ApiRequestState.IDLE)
    val uiStateStateFlow: Flow<ApiRequestState> = uiStateStateFlowInternal
    val uiStateState: LiveData<ApiRequestState> = uiStateStateFlow.asLiveData()

    private val testResultReceivedDateFlowInternal = MutableStateFlow(Date())
    val testResultReceivedDateFlow: Flow<Date> = testResultReceivedDateFlowInternal
    val testResultReceivedDate = testResultReceivedDateFlow.asLiveData()

    private val deviceUIStateFlowInternal = MutableStateFlow(DeviceUIState.UNPAIRED)
    val deviceUIStateFlow: Flow<DeviceUIState> = deviceUIStateFlowInternal
    val deviceUIState = deviceUIStateFlow.asLiveData()

    private val testResultFlow = MutableStateFlow<TestResult?>(null)

    private suspend fun fetchTestResult(): DeviceUIState = try {
        val testResult = SubmissionService.asyncRequestTestResult()
        updateTestResult(testResult)
        deriveUiState(testResult)
    } catch (err: NoRegistrationTokenSetException) {
        DeviceUIState.UNPAIRED
    }

    fun updateTestResult(testResult: TestResult) {
        this.testResultFlow.value = testResult

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

    private fun deriveUiState(testResult: TestResult?): DeviceUIState = when (testResult) {
        TestResult.NEGATIVE -> DeviceUIState.PAIRED_NEGATIVE
        TestResult.POSITIVE -> DeviceUIState.PAIRED_POSITIVE
        TestResult.PENDING -> DeviceUIState.PAIRED_NO_RESULT
        TestResult.REDEEMED -> DeviceUIState.PAIRED_REDEEMED
        TestResult.INVALID -> DeviceUIState.PAIRED_ERROR
        null -> DeviceUIState.UNPAIRED
    }

    fun setTeletan(teletan: String) {
        LocalData.teletan(teletan)
    }

    private val uiStateErrorInternal = MutableLiveData<Event<CwaWebException>>(null)
    val uiStateError: LiveData<Event<CwaWebException>> = uiStateErrorInternal

    // TODO this should be more UI agnostic
    fun refreshDeviceUIState(refreshTestResult: Boolean = true) {
        var refresh = refreshTestResult

        deviceUIStateFlowInternal.value.let {
            if (it != DeviceUIState.PAIRED_NO_RESULT && it != DeviceUIState.UNPAIRED) {
                refresh = false
                Timber.d("refreshDeviceUIState: Change refresh, state ${it.name} doesn't require refresh")
            }
        }

        uiStateStateFlowInternal.value = ApiRequestState.STARTED
        appScope.launch {
            try {
                refreshUIState(refresh)
                uiStateStateFlowInternal.value = ApiRequestState.SUCCESS
            } catch (err: CwaWebException) {
                uiStateErrorInternal.postValue(Event(err))
                uiStateStateFlowInternal.value = ApiRequestState.FAILED
            } catch (err: Exception) {
                err.report(ExceptionCategory.INTERNAL)
            }
        }
    }

    // TODO this should be more UI agnostic
    private suspend fun refreshUIState(refreshTestResult: Boolean) {
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
                        deriveUiState(testResultFlow.value)
                    }
                }
            }
        }
        deviceUIStateFlowInternal.value = uiState
    }
}
