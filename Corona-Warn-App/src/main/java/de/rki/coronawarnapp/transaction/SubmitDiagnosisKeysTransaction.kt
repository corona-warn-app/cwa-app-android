package de.rki.coronawarnapp.transaction

import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyService
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.CLOSE
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.SUBMIT_KEYS
import de.rki.coronawarnapp.util.ProtoFormatConverterExtensions.transformKeyHistoryToExternalFormat

/**
 * The ReportDiagnosisKeysTransaction is used to define an atomic Transaction for Key Reports. Its states allow an
 * isolated work area that can recover from failures and keep a consistent key state even through an
 * unclear, potentially dangerous state within the transaction itself. It is guaranteed that the Key Files
 * that are used in the transaction will be generated, submitted and accepted from the Google API once the transaction
 * has completed its work and returned from the start() coroutine.
 *
 * There is currently a simple rollback behavior needed / identified.
 *
 * The Transaction undergoes multiple States:
 * 1. INIT - Initial Setup of the Transaction and Transaction ID Generation
 * 2. TOKEN - Initialisation of the identifying token used during the entire transaction
 * 3. RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY - Retrieval of the Exposure Key History from the API
 * 5. SUBMIT_KEYS - Submission of Key retrieved to the Server
 * 8. CLOSE - Transaction Closure
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
        SUBMIT_KEYS,
        CLOSE
    }

    /** initiates the transaction. This suspend function guarantees a successful transaction once completed. */
    suspend fun start(authCode: String) = lockAndExecuteUnique {
        /****************************************************
         * RETRIEVE TEMPORARY EXPOSURE KEY HISTORY
         ****************************************************/
        val temporaryExposureKeyList = executeState(RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY) {
            InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory()
                .transformKeyHistoryToExternalFormat()
        }
        /****************************************************
         * SUBMIT KEYS
         ****************************************************/
        executeState(SUBMIT_KEYS) {
            DiagnosisKeyService.asyncSubmitKeys(authCode, temporaryExposureKeyList)
        }
        /****************************************************
         * CLOSE TRANSACTION
         ****************************************************/
        executeState(CLOSE) {}
    }
}
