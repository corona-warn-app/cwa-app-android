package de.rki.coronawarnapp.service.submission

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.exception.NoGUIDOrTANSetException
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.http.playbook.BackgroundNoise
import de.rki.coronawarnapp.http.playbook.PlaybookImpl
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler

object SubmissionService {

    suspend fun asyncRegisterDevice() {
        val testGUID = LocalData.testGUID()
        val testTAN = LocalData.teletan()

        when {
            testGUID != null -> asyncRegisterDeviceViaGUID(testGUID)
            testTAN != null -> asyncRegisterDeviceViaTAN(testTAN)
            else -> throw NoGUIDOrTANSetException()
        }
        LocalData.devicePairingSuccessfulTimestamp(System.currentTimeMillis())
        BackgroundNoise.getInstance().scheduleDummyPattern()
    }

    private suspend fun asyncRegisterDeviceViaGUID(guid: String) {
        val (registrationToken, testResult) =
            PlaybookImpl(WebRequestBuilder.getInstance()).initialRegistration(
                guid,
                KeyType.GUID
            )

        LocalData.registrationToken(registrationToken)
        deleteTestGUID()
        SubmissionRepository.updateTestResult(testResult)
    }

    private suspend fun asyncRegisterDeviceViaTAN(tan: String) {
        val (registrationToken, testResult) =
            PlaybookImpl(WebRequestBuilder.getInstance()).initialRegistration(
                tan,
                KeyType.TELETAN
            )

        LocalData.registrationToken(registrationToken)
        deleteTeleTAN()
        SubmissionRepository.updateTestResult(testResult)
    }

    suspend fun asyncSubmitExposureKeys(keys: List<TemporaryExposureKey>) {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
        SubmitDiagnosisKeysTransaction.start(registrationToken, keys)
    }

    suspend fun asyncRequestTestResult(): TestResult {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()

        return PlaybookImpl(WebRequestBuilder.getInstance()).testResult(registrationToken)
    }

    fun containsValidGUID(scanResult: String): Boolean {
        val scanResult = ScanResult(scanResult)
        return scanResult.isValid
    }

    fun storeTestGUID(guid: String) = LocalData.testGUID(guid)

    fun deleteTestGUID() {
        LocalData.testGUID(null)
    }

    fun deleteRegistrationToken() {
        LocalData.registrationToken(null)
        LocalData.devicePairingSuccessfulTimestamp(0L)
    }

    fun submissionSuccessful() {
        BackgroundWorkScheduler.stopWorkScheduler()
        LocalData.numberOfSuccessfulSubmissions(1)
    }

    private fun deleteTeleTAN() {
        LocalData.teletan(null)
    }
}
