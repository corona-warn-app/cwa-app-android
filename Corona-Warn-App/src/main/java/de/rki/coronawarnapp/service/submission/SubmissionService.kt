package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.exception.NoGUIDOrTANSetException
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.SubmissionConstants.QR_CODE_KEY_TYPE
import de.rki.coronawarnapp.service.submission.SubmissionConstants.REGISTRATION_TOKEN_URL
import de.rki.coronawarnapp.service.submission.SubmissionConstants.TAN_REQUEST_URL
import de.rki.coronawarnapp.service.submission.SubmissionConstants.TELE_TAN_KEY_TYPE
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction

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
    }

    private suspend fun asyncRegisterDeviceViaGUID(guid: String) {
        val registrationToken =
            WebRequestBuilder.asyncGetRegistrationToken(
                REGISTRATION_TOKEN_URL,
                guid,
                QR_CODE_KEY_TYPE
            )

        LocalData.registrationToken(registrationToken)
        deleteTestGUID()
    }

    private suspend fun asyncRegisterDeviceViaTAN(tan: String) {
        val registrationToken =
            WebRequestBuilder.asyncGetRegistrationToken(
                REGISTRATION_TOKEN_URL,
                tan,
                TELE_TAN_KEY_TYPE
            )

        LocalData.registrationToken(registrationToken)
        deleteTeleTAN()
    }

    suspend fun asyncRequestAuthCode(): String {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()

        val authCode = WebRequestBuilder.asyncGetTan(TAN_REQUEST_URL, registrationToken)
        return authCode
    }

    suspend fun asyncSubmitExposureKeys() {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
        SubmitDiagnosisKeysTransaction.start(registrationToken)
    }

    /**
     * extracts the GUID from [scanResult]. Returns null if it does not match the required pattern
     */
    fun extractGUID(scanResult: String): String? {
        val potentialGUID = scanResult.substringAfterLast("?", "")
        return if (potentialGUID.isEmpty())
            null
        else
            potentialGUID
    }

    fun storeTestGUID(guid: String) = LocalData.testGUID(guid)

    fun deleteTestGUID() {
        LocalData.testGUID(null)
    }

    fun deleteRegistrationToken() {
        LocalData.registrationToken(null)
        LocalData.devicePairingSuccessfulTimestamp(0L)
    }

    private fun deleteAuthCode() {
        LocalData.authCode(null)
    }

    private fun deleteTeleTAN() {
        LocalData.teletan(null)
    }
}
