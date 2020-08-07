package de.rki.coronawarnapp.storage

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import java.util.Date

object SubmissionRepository {
    private val TAG: String? = SubmissionRepository::class.simpleName

    val testResultReceivedDate = MutableLiveData(Date())
    val deviceUIState = MutableLiveData(DeviceUIState.UNPAIRED)

    suspend fun refreshUIState() {
        var uiState = DeviceUIState.UNPAIRED

        if (LocalData.numberOfSuccessfulSubmissions() == 1) {
            uiState = DeviceUIState.SUBMITTED_FINAL
        } else {
            if (LocalData.registrationToken() != null) {
                uiState = when {
                    LocalData.isAllowedToSubmitDiagnosisKeys() == true -> {
                        DeviceUIState.PAIRED_POSITIVE
                    }
                    else -> fetchTestResult()
                }
            }
        }
        deviceUIState.value = uiState
    }

    private suspend fun fetchTestResult(): DeviceUIState {
        try {
            val testResult = SubmissionService.asyncRequestTestResult()

            if (testResult == TestResult.POSITIVE) {
                LocalData.isAllowedToSubmitDiagnosisKeys(true)
            }

            val initialTestResultReceivedTimestamp = LocalData.initialTestResultReceivedTimestamp()

            if (initialTestResultReceivedTimestamp == null) {
                val currentTime = System.currentTimeMillis()
                LocalData.initialTestResultReceivedTimestamp(currentTime)
                testResultReceivedDate.value = Date(currentTime)
                if (testResult == TestResult.PENDING) {
                    BackgroundWorkScheduler.startWorkScheduler()
                }
            } else {
                testResultReceivedDate.value = Date(initialTestResultReceivedTimestamp)
            }

            return when (testResult) {
                TestResult.NEGATIVE -> DeviceUIState.PAIRED_NEGATIVE
                TestResult.POSITIVE -> DeviceUIState.PAIRED_POSITIVE
                TestResult.PENDING -> DeviceUIState.PAIRED_NO_RESULT
                TestResult.INVALID -> DeviceUIState.PAIRED_ERROR
                TestResult.REDEEMED -> DeviceUIState.PAIRED_REDEEMED
            }
        } catch (err: NoRegistrationTokenSetException) {
            return DeviceUIState.UNPAIRED
        }
    }

    fun setTeletan(teletan: String) {
        LocalData.teletan(teletan)
    }
}
