package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.exception.InvalidQRCodeExcpetion
import de.rki.coronawarnapp.exception.NoAuthCodeSetException
import de.rki.coronawarnapp.exception.NoGUIDOrTANSetException
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.SubmissionConstants.QR_CODE_KEY_TYPE
import de.rki.coronawarnapp.service.submission.SubmissionConstants.QR_CODE_VALIDATION_REGEX
import de.rki.coronawarnapp.service.submission.SubmissionConstants.REGISTRATION_TOKEN_URL
import de.rki.coronawarnapp.service.submission.SubmissionConstants.TAN_REQUEST_URL
import de.rki.coronawarnapp.service.submission.SubmissionConstants.TELE_TAN__KEY_TYPE
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
                TELE_TAN__KEY_TYPE
            )

        LocalData.registrationToken(registrationToken)
        deleteTeleTAN()
    }

    suspend fun asyncRequestAuthCode() {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()

        val authCode = WebRequestBuilder.asyncGetTan(TAN_REQUEST_URL, registrationToken)

        LocalData.authCode(authCode)
        deleteRegistrationToken()
    }

    suspend fun asyncSubmitExposureKeys() {
        val authCode =
            LocalData.authCode() ?: throw NoAuthCodeSetException()

        SubmitDiagnosisKeysTransaction.start(authCode)

        deleteAuthCode()
    }

    fun validateAndStoreTestGUID(testGUID: String) {
        val regexMatch = QR_CODE_VALIDATION_REGEX.find(testGUID) ?: throw InvalidQRCodeExcpetion()
        LocalData.testGUID(regexMatch.value)
    }

    fun deleteTestGUID() {
        LocalData.testGUID(null)
    }

    fun deleteRegistrationToken() {
        LocalData.registrationToken(null)
    }

    private fun deleteAuthCode() {
        LocalData.authCode(null)
    }

    private fun deleteTeleTAN() {
        LocalData.teletan(null)
    }
}
