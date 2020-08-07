package de.rki.coronawarnapp.transaction

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.http.playbook.PlaybookImpl
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.CLOSE
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.RETRIEVE_TAN_AND_SUBMIT_KEYS
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.STORE_SUCCESS
import de.rki.coronawarnapp.util.ProtoFormatConverterExtensions.limitKeyCount
import de.rki.coronawarnapp.util.ProtoFormatConverterExtensions.transformKeyHistoryToExternalFormat

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
object SubmitDiagnosisKeysTransaction : Transaction() {

    override val TAG: String? = SubmitDiagnosisKeysTransaction::class.simpleName

    /** possible transaction states */
    private enum class SubmitDiagnosisKeysTransactionState :
        TransactionState {
        RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY,
        RETRIEVE_TAN_AND_SUBMIT_KEYS,
        STORE_SUCCESS,
        CLOSE
    }

    /** initiates the transaction. This suspend function guarantees a successful transaction once completed. */
    suspend fun start(
        registrationToken: String,
        keys: List<TemporaryExposureKey>
    ) = lockAndExecuteUnique {
        /****************************************************
         * RETRIEVE TEMPORARY EXPOSURE KEY HISTORY
         ****************************************************/
        val temporaryExposureKeyList = executeState(RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY) {
                keys.limitKeyCount()
                .transformKeyHistoryToExternalFormat()
        }
        /****************************************************
         * RETRIEVE TAN & SUBMIT KEYS
         ****************************************************/
        executeState(RETRIEVE_TAN_AND_SUBMIT_KEYS) {
            PlaybookImpl(WebRequestBuilder.getInstance()).submission(
                registrationToken,
                temporaryExposureKeyList
            )
        }
        /****************************************************
         * STORE SUCCESS
         ****************************************************/
        executeState(STORE_SUCCESS) {
            SubmissionService.submissionSuccessful()
        }
        /****************************************************
         * CLOSE TRANSACTION
         ****************************************************/
        executeState(CLOSE) {}
    }
}
