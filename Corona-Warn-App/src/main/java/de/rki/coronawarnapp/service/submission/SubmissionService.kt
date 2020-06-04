package de.rki.coronawarnapp.service.submission

import com.android.volley.VolleyError
import de.rki.coronawarnapp.exception.NoGUIDOrTANSetException
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.exception.TestAlreadyPairedException
import de.rki.coronawarnapp.exception.TestPairingInvalidException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.SubmissionConstants.QR_CODE_KEY_TYPE
import de.rki.coronawarnapp.service.submission.SubmissionConstants.TELE_TAN_KEY_TYPE
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.formatter.TestResult

object SubmissionService {
    suspend fun asyncRegisterDevice() {
        try {
            val testGUID = LocalData.testGUID()
            val testTAN = LocalData.teletan()

            when {
                testGUID != null -> asyncRegisterDeviceViaGUID(testGUID)
                testTAN != null -> asyncRegisterDeviceViaTAN(testTAN)
                else -> throw NoGUIDOrTANSetException()
            }
            LocalData.devicePairingSuccessfulTimestamp(System.currentTimeMillis())
        } catch (err: Exception) {
            var cause = err.cause

            if (cause is VolleyError) {
                if (cause.networkResponse?.statusCode == 400) {
                    cause = TestAlreadyPairedException(
                        "the test was already paired to a different device",
                        cause
                    )
                }
            }

            throw cause ?: err
        }
    }

    private suspend fun asyncRegisterDeviceViaGUID(guid: String) {
        val registrationToken =
            WebRequestBuilder.asyncGetRegistrationToken(
                guid,
                QR_CODE_KEY_TYPE
            )

        LocalData.registrationToken(registrationToken)
        deleteTestGUID()
    }

    private suspend fun asyncRegisterDeviceViaTAN(tan: String) {
        val registrationToken =
            WebRequestBuilder.asyncGetRegistrationToken(
                tan,
                TELE_TAN_KEY_TYPE
            )

        LocalData.registrationToken(registrationToken)
        deleteTeleTAN()
    }

    suspend fun asyncRequestAuthCode(registrationToken: String): String {
        try {
            return WebRequestBuilder.asyncGetTan(TAN_REQUEST_URL, registrationToken)
        } catch (err: Exception) {
            var cause = err.cause

            if (cause is VolleyError) {
                if (cause.networkResponse?.statusCode == 400) {
                    cause = TestPairingInvalidException(
                        "the test paring to the device is invalid",
                        cause
                    )
                }
            }

            throw cause ?: err
        }
    }

    suspend fun asyncSubmitExposureKeys() {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
        SubmitDiagnosisKeysTransaction.start(registrationToken)
    }

    suspend fun asyncRequestTestResult(): TestResult {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
        return TestResult.fromInt(
            WebRequestBuilder.asyncGetTestResult(registrationToken)
        )
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

    private fun deleteTeleTAN() {
        LocalData.teletan(null)
    }
}
