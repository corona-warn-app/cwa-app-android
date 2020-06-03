package de.rki.coronawarnapp.storage

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.util.formatter.TestResult
import java.util.Date

object SubmissionRepository {
    private val TAG: String? = SubmissionRepository::class.simpleName

    val testResult = MutableLiveData(TestResult.INVALID)
    val testResultReceivedDate = MutableLiveData(Date())

    suspend fun refreshTestResult() {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
        val testResultValue = WebRequestBuilder.asyncGetTestResult(registrationToken)
        testResult.value = TestResult.fromInt(testResultValue)
        if (testResult == TestResult.POSITIVE) {
            LocalData.isAllowedToSubmitDiagnosisKeys(true)
        }
        val initialTestResultReceivedTimestamp = LocalData.inititalTestResultReceivedTimestamp()

        if (initialTestResultReceivedTimestamp == null) {
            val currentTime = System.currentTimeMillis()
            LocalData.inititalTestResultReceivedTimestamp(currentTime)
            testResultReceivedDate.value = Date(currentTime)
        } else {
            testResultReceivedDate.value = Date(initialTestResultReceivedTimestamp)
        }
    }

    fun setTeletan(teletan: String) {
        LocalData.teletan(teletan)
    }
}
