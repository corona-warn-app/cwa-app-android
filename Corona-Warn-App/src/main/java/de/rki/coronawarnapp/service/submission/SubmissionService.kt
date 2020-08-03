package de.rki.coronawarnapp.service.submission

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.exception.NoGUIDOrTANSetException
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.http.playbook.BackgroundNoise
import de.rki.coronawarnapp.http.playbook.PlaybookImpl
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.CoroutineScope

object SubmissionService {

    suspend fun asyncRegisterDevice(coroutineScope: CoroutineScope) {
        val testGUID = LocalData.testGUID()
        val testTAN = LocalData.teletan()

        when {
            testGUID != null -> asyncRegisterDeviceViaGUID(coroutineScope, testGUID)
            testTAN != null -> asyncRegisterDeviceViaTAN(coroutineScope, testTAN)
            else -> throw NoGUIDOrTANSetException()
        }
        LocalData.devicePairingSuccessfulTimestamp(System.currentTimeMillis())
        BackgroundNoise.getInstance().scheduleDummyPattern()
    }

    private suspend fun asyncRegisterDeviceViaGUID(coroutineScope: CoroutineScope, guid: String) {
        val registrationToken =
            PlaybookImpl(WebRequestBuilder.getInstance(), coroutineScope).initialRegistration(
                guid,
                KeyType.GUID
            )
//        WebRequestBuilder.getInstance().asyncGetRegistrationToken(
//            guid,
//            QR_CODE_KEY_TYPE
//        )

        LocalData.registrationToken(registrationToken)
        deleteTestGUID()
    }

    private suspend fun asyncRegisterDeviceViaTAN(coroutineScope: CoroutineScope, tan: String) {
        val registrationToken =
            PlaybookImpl(WebRequestBuilder.getInstance(), coroutineScope).initialRegistration(
                tan,
                KeyType.TELETAN
            )
//            WebRequestBuilder.getInstance().asyncGetRegistrationToken(
//                tan,
//                TELE_TAN_KEY_TYPE
//            )

        LocalData.registrationToken(registrationToken)
        deleteTeleTAN()
    }

    suspend fun asyncSubmitExposureKeys(coroutineScope: CoroutineScope, keys: List<TemporaryExposureKey>) {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
        SubmitDiagnosisKeysTransaction.start(coroutineScope, registrationToken, keys)
    }

    suspend fun asyncRequestTestResult(coroutineScope: CoroutineScope): TestResult {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()

        return PlaybookImpl(WebRequestBuilder.getInstance(), coroutineScope).testResult(registrationToken)
//        return TestResult.fromInt(
//            WebRequestBuilder.getInstance().asyncGetTestResult(registrationToken)
//        )
    }

    fun containsValidGUID(scanResult: String): Boolean {
        if (scanResult.length > SubmissionConstants.MAX_QR_CODE_LENGTH ||
            scanResult.count { it == SubmissionConstants.GUID_SEPARATOR } != 1
        )
            return false

        val potentialGUID = extractGUID(scanResult)

        return !(potentialGUID.isEmpty() || potentialGUID.length > SubmissionConstants.MAX_GUID_LENGTH)
    }

    fun extractGUID(scanResult: String): String =
        scanResult.substringAfterLast(SubmissionConstants.GUID_SEPARATOR, "")

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
