package de.rki.coronawarnapp.storage

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.Event
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmissionRepository @Inject constructor(
    private val submissionSettings: SubmissionSettings,
    private val submissionService: SubmissionService,
    @AppScope private val scope: CoroutineScope,
    private val timeStamper: TimeStamper
) {

    companion object {
        fun submissionSuccessful() {
            BackgroundWorkScheduler.stopWorkScheduler()
            LocalData.numberOfSuccessfulSubmissions(1)
        }
        fun deleteRegistrationToken() {
            LocalData.registrationToken(null)
            LocalData.devicePairingSuccessfulTimestamp(0L)
        }
    }

    private val uiStateErrorInternal = MutableLiveData<Event<CwaWebException>>(null)
    val uiStateError: LiveData<Event<CwaWebException>> = uiStateErrorInternal

    private val uiStateStateFlowInternal = MutableStateFlow(ApiRequestState.IDLE)
    val uiStateStateFlow: Flow<ApiRequestState> = uiStateStateFlowInternal

    private val testResultReceivedDateFlowInternal = MutableStateFlow(Date())
    val testResultReceivedDateFlow: Flow<Date> = testResultReceivedDateFlowInternal

    private val deviceUIStateFlowInternal = MutableStateFlow(DeviceUIState.UNPAIRED)
    val deviceUIStateFlow: Flow<DeviceUIState> = deviceUIStateFlowInternal

    // to be used by new submission flow screens
    val hasGivenConsentToSubmission = submissionSettings.hasGivenConsent.flow

    private val testResultFlow = MutableStateFlow<TestResult?>(null)

    fun setTeletan(teletan: String) {
        LocalData.teletan(teletan)
    }

    fun deleteTestGUID() {
        LocalData.testGUID(null)
    }

    // to be used by new submission flow screens
    fun giveConsentToSubmission() {
        submissionSettings.hasGivenConsent.update {
            true
        }
    }

    // to be used by new submission flow screens
    fun revokeConsentToSubmission() {
        submissionSettings.hasGivenConsent.update {
            false
        }
    }

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
        scope.launch {
            try {
                deviceUIStateFlowInternal.value = refreshUIState(refresh)
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
    suspend fun refreshUIState(refreshTestResult: Boolean): DeviceUIState {
        var uiState = DeviceUIState.UNPAIRED

        if (LocalData.submissionWasSuccessful()) {
            uiState = DeviceUIState.SUBMITTED_FINAL
        } else {
            val registrationToken = LocalData.registrationToken()
            if (registrationToken != null) {
                uiState = when {
                    LocalData.isAllowedToSubmitDiagnosisKeys() == true -> {
                        DeviceUIState.PAIRED_POSITIVE
                    }
                    refreshTestResult -> fetchTestResult(registrationToken)
                    else -> {
                        deriveUiState(testResultFlow.value)
                    }
                }
            }
        }
        return uiState
    }

    suspend fun asyncRegisterDeviceViaGUID(guid: String): TestResult {
        val registrationData = submissionService.asyncRegisterDeviceViaGUID(guid)
        LocalData.registrationToken(registrationData.registrationToken)
        LocalData.testGUID(null)
        updateTestResult(registrationData.testResult)
        LocalData.devicePairingSuccessfulTimestamp(timeStamper.nowUTC.millis)
        BackgroundNoise.getInstance().scheduleDummyPattern()
        return registrationData.testResult
    }

    suspend fun asyncRegisterDeviceViaTAN(tan: String) {
        val registrationData = submissionService.asyncRegisterDeviceViaTAN(tan)
        LocalData.registrationToken(registrationData.registrationToken)
        LocalData.teletan(null)
        updateTestResult(registrationData.testResult)
        LocalData.devicePairingSuccessfulTimestamp(timeStamper.nowUTC.millis)
        BackgroundNoise.getInstance().scheduleDummyPattern()
    }

    fun reset() {
        uiStateStateFlowInternal.value = ApiRequestState.IDLE
        deviceUIStateFlowInternal.value = DeviceUIState.UNPAIRED
        revokeConsentToSubmission()
    }

    @VisibleForTesting
    fun updateTestResult(testResult: TestResult) {
        testResultFlow.value = testResult

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

    private suspend fun fetchTestResult(registrationToken: String): DeviceUIState = try {
        val testResult = submissionService.asyncRequestTestResult(registrationToken)
        updateTestResult(testResult)
        deriveUiState(testResult)
    } catch (err: NoRegistrationTokenSetException) {
        DeviceUIState.UNPAIRED
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
