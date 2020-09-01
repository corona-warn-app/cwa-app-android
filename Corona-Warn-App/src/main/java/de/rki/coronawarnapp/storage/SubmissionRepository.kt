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
    val visitedCountries: MutableLiveData<List<String>> = MutableLiveData()
    val consentToFederation = MutableLiveData(false)
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
        deviceUIState.value = uiState
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
            testResultReceivedDate.value = Date(currentTime)
            if (testResult == TestResult.PENDING) {
                BackgroundWorkScheduler.startWorkScheduler()
            }
        } else {
            testResultReceivedDate.value = Date(initialTestResultReceivedTimestamp)
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

    fun setVisitedCountries(countries: List<String>) {
        visitedCountries.postValue(countries)
    }

    fun setConsentToFederation(consent: Boolean) {
        consentToFederation.postValue(consent)
    }
}
