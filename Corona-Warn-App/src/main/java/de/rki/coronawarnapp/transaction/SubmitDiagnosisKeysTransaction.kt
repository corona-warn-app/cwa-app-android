package de.rki.coronawarnapp.transaction

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.http.requests.TanRequestBody
import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.CLOSE
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.RETRIEVE_TAN
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.STORE_SUCCESS
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.SUBMIT_KEYS
import de.rki.coronawarnapp.util.ProtoFormatConverterExtensions.limitKeyCount
import de.rki.coronawarnapp.util.ProtoFormatConverterExtensions.transformKeyHistoryToExternalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The SubmitDiagnosisKeysTransaction is used to define an atomic Transaction for Key Reports. Its states allow an
 * isolated work area that can recover from failures and keep a consistent key state even through an
 * unclear, potentially dangerous state within the transaction itself. It is guaranteed that the Key Files
 * that are used in the transaction will be generated, submitted and accepted from the Google API once the transaction
 * has completed its work and returned from the start() coroutine.
 *
 * There is currently a simple rollback behavior needed / identified.
 *
 * The Transaction undergoes multiple States:
 * 1. RETRIEVE_TAN - Fetch the TAN with the provided Registration Token
 * 2. RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY - Get the TEKs from the exposure notification framework
 * 3. SUBMIT_KEYS - Submission of the diagnosis keys to the Server
 * 4. CLOSE - Transaction Closure
 *
 * This transaction is special in terms of concurrent entry-calls (e.g. calling the transaction again before it closes and
 * releases its internal mutex. The transaction will not queue up like a normal mutex, but instead completely omit the last
 * execution. Execution Privilege is First In.
 *
 * @see Transaction
 *
 * @throws de.rki.coronawarnapp.exception.TransactionException An Exception thrown when an error occurs during Transaction Execution
 */
@Singleton
class SubmitDiagnosisKeysTransaction @Inject constructor(
    val submissionService: SubmissionService,
    val verificationService: VerificationService
) : Transaction() {

    override val TAG: String? = SubmitDiagnosisKeysTransaction::class.simpleName

    /** possible transaction states */
    private enum class SubmitDiagnosisKeysTransactionState :
        TransactionState {
        RETRIEVE_TAN,
        RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY,
        SUBMIT_KEYS,
        STORE_SUCCESS,
        CLOSE
    }

    /** initiates the transaction. This suspend function guarantees a successful transaction once completed. */
    suspend fun start(registrationToken: String, keys: List<TemporaryExposureKey>) = lockAndExecuteUnique {
        /****************************************************
         * RETRIEVE TAN
         ****************************************************/
        val authCode = executeState(RETRIEVE_TAN) {
            asyncRequestAuthCode(registrationToken)
        }

        /****************************************************
         * RETRIEVE TEMPORARY EXPOSURE KEY HISTORY
         ****************************************************/
        val temporaryExposureKeyList = executeState(RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY) {
                keys.limitKeyCount()
                .transformKeyHistoryToExternalFormat()
        }
        /****************************************************
         * SUBMIT KEYS
         ****************************************************/
        executeState(SUBMIT_KEYS) {
            asyncSubmitKeys(authCode, temporaryExposureKeyList)
        }
        /****************************************************
         * STORE SUCCESS
         ****************************************************/
        executeState(STORE_SUCCESS) {
            setSubmissionSuccessful()
        }
        /****************************************************
         * CLOSE TRANSACTION
         ****************************************************/
        executeState(CLOSE) {}
    }

    private suspend fun asyncRequestAuthCode(registrationToken: String): String {
        return withContext(Dispatchers.IO) {
            verificationService.getTAN(
                SubmissionConstants.TAN_REQUEST_URL, "0",
                TanRequestBody(registrationToken)
            ).tan
        }
    }

    private fun setSubmissionSuccessful() {
        LocalData.numberOfSuccessfulSubmissions(1)
    }

    private suspend fun asyncSubmitKeys(
        authCode: String,
        keysToReport: List<KeyExportFormat.TemporaryExposureKey>
    ) {
        Timber.d("Diagnosis Keys will be submitted.")
        withContext(Dispatchers.IO) {
            Timber.d("Writing ${keysToReport.size} Keys to the Submission Payload.")
            val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
                .addAllKeys(keysToReport)
                .build()
            var fakeHeader = "0"
            if (false) fakeHeader = Math.random().toInt().toString()
            submissionService.submitKeys(
                DiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL,
                authCode,
                fakeHeader,
                submissionPayload
            )
            return@withContext
        }
    }
}
