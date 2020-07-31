/******************************************************************************
 * Corona-Warn-App                                                            *
 *                                                                            *
 * SAP SE and all other contributors /                                        *
 * copyright owners license this file to you under the Apache                 *
 * License, Version 2.0 (the "License"); you may not use this                 *
 * file except in compliance with the License.                                *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing,                 *
 * software distributed under the License is distributed on an                *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                     *
 * KIND, either express or implied.  See the License for the                  *
 * specific language governing permissions and limitations                    *
 * under the License.                                                         *
 ******************************************************************************/

package de.rki.coronawarnapp.transaction

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.FileStorageHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.keycache.KeyCacheRepository
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction.RetrieveDiagnosisKeysTransactionState.API_SUBMISSION
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction.RetrieveDiagnosisKeysTransactionState.CLOSE
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction.RetrieveDiagnosisKeysTransactionState.FETCH_DATE_UPDATE
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction.RetrieveDiagnosisKeysTransactionState.FILES_FROM_WEB_REQUESTS
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction.RetrieveDiagnosisKeysTransactionState.RETRIEVE_RISK_SCORE_PARAMS
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction.RetrieveDiagnosisKeysTransactionState.SETUP
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction.RetrieveDiagnosisKeysTransactionState.TOKEN
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction.rollback
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction.start
import de.rki.coronawarnapp.util.CachedKeyFileHolder
import de.rki.coronawarnapp.worker.BackgroundWorkHelper
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import timber.log.Timber
import java.io.File
import java.util.Date
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

/**
 * The RetrieveDiagnosisKeysTransaction is used to define an atomic Transaction for Key Retrieval. Its states allow an
 * isolated work area that can recover from failures and keep a consistent key state even through an
 * unclear, potentially dangerous state within the transaction itself. It is guaranteed that the keys used in the
 * transaction will be successfully retrieved from the Google API and accepted by the server and that the transaction
 * has completed its work after completing the [start] coroutine.
 *
 * It has to be noted that this is not a real, but a transient transaction that does not have an explicit commit stage.
 * As such we do not define an execution plan, but rather commit in each transaction state and do a [rollback] based
 * on given state. (given that we do not operate on a database layer but on a business logic requiring states to be run
 * asynchronously and in a distributed context)
 *
 * There is currently a simple rollback behavior defined for this transaction. This means that persisted files up until
 * that point are deleted. Also, The last fetch date will be rolled back on best effort base and the Google API token
 * reset if necessary.
 *
 * The Transaction undergoes multiple States:
 * 1. [SETUP]
 * 2. [TOKEN]
 * 3. [RETRIEVE_RISK_SCORE_PARAMS]
 * 4. [FILES_FROM_WEB_REQUESTS]
 * 5. [API_SUBMISSION]
 * 6. [FETCH_DATE_UPDATE]
 * 7. [CLOSE]
 *
 * This transaction is special in terms of concurrent entry-calls (e.g. calling the transaction again before it closes and
 * releases its internal mutex. The transaction will not queue up like a normal mutex, but instead completely omit the last
 * execution. Execution Privilege is First In.
 *
 * @see Transaction
 *
 * @throws de.rki.coronawarnapp.exception.TransactionException An Exception thrown when an error occurs during Transaction Execution
 * @throws de.rki.coronawarnapp.exception.RollbackException An Exception thrown when an error occurs during Rollback of the Transaction
 */
object RetrieveDiagnosisKeysTransaction : Transaction() {

    override val TAG: String? = RetrieveDiagnosisKeysTransaction::class.simpleName

    /** possible transaction states */
    private enum class RetrieveDiagnosisKeysTransactionState :
        TransactionState {
        /** Initial Setup of the Transaction and Transaction ID Generation and Date Lock */
        SETUP,

        /** Initialisation of the identifying token used during the entire transaction */
        TOKEN,

        /** Retrieval of Risk Score Parameters used for the Key Submission to the Google API */
        RETRIEVE_RISK_SCORE_PARAMS,

        /** Retrieval of actual Key Files based on the URLs */
        FILES_FROM_WEB_REQUESTS,

        /** Submission of parsed KeyFiles into the Google API */
        API_SUBMISSION,

        /** Update of the Fetch Date to reflect a complete Transaction State */
        FETCH_DATE_UPDATE,

        /** Transaction Closure */
        CLOSE
    }

    /** atomic reference for the rollback value for the last fetch date */
    private val lastFetchDateForRollback = AtomicReference<Date>()

    /** atomic reference for the rollback value for the google api */
    private val googleAPITokenForRollback = AtomicReference<String>()

    /** atomic reference for the rollback value for created files during the transaction */
    private val exportFilesForRollback = AtomicReference<List<File>>()

    suspend fun startWithConstraints() {
        val currentDate = DateTime(Instant.now(), DateTimeZone.UTC)
        val lastFetch = DateTime(
            LocalData.lastTimeDiagnosisKeysFromServerFetch(),
            DateTimeZone.UTC
        )
        if (LocalData.lastTimeDiagnosisKeysFromServerFetch() == null ||
            currentDate.withTimeAtStartOfDay() != lastFetch.withTimeAtStartOfDay()
        ) {
            BackgroundWorkHelper.sendDebugNotification(
                "Start RetrieveDiagnosisKeysTransaction",
                "No keys fetched today yet \n${DateTime.now()}\nUTC: $currentDate"
            )
            start()
        }
    }

    /** initiates the transaction. This suspend function guarantees a successful transaction once completed. */
    suspend fun start() = lockAndExecuteUnique {
        /**
         * Handles the case when the ENClient got disabled but the Transaction is still scheduled
         * in a background job. Also it acts as a failure catch in case the orchestration code did
         * not check in before.
         */
        if (!InternalExposureNotificationClient.asyncIsEnabled()) {
            Timber.w("EN is not enabled, skipping RetrieveDiagnosisKeys")
            executeClose()
            return@lockAndExecuteUnique
        }
        /****************************************************
         * INIT TRANSACTION
         ****************************************************/
        val currentDate = executeSetup()

        /****************************************************
         * RETRIEVE TOKEN
         ****************************************************/
        val token = executeToken()

        /****************************************************
         * RETRIEVE RISK SCORE PARAMETERS
         ****************************************************/
        val exposureConfiguration = executeRetrieveRiskScoreParams()

        /****************************************************
         * FILES FROM WEB REQUESTS
         ****************************************************/
        val keyFiles = executeFetchKeyFilesFromServer(currentDate)

        if (keyFiles.isNotEmpty()) {
            /****************************************************
             * SUBMIT FILES TO API
             ****************************************************/
            executeAPISubmission(token, keyFiles, exposureConfiguration)
        } else {
            Timber.w("no key files, skipping submission to internal API.")
        }
        /****************************************************
         * Fetch Date Update
         ****************************************************/
        executeFetchDateUpdate(currentDate)
        /****************************************************
         * CLOSE TRANSACTION
         ****************************************************/
        executeClose()
    }

    override suspend fun rollback() {
        super.rollback()
        try {
            if (SETUP.isInStateStack()) {
                rollbackSetup()
            }
            if (TOKEN.isInStateStack()) {
                rollbackToken()
            }
            if (FILES_FROM_WEB_REQUESTS.isInStateStack()) {
                rollbackFilesFromWebRequests()
            }
        } catch (e: Exception) {
            // We handle every exception through a RollbackException to make sure that a single EntryPoint
            // is available for the caller.
            handleRollbackError(e)
        }
    }

    private fun rollbackSetup() {
        Timber.v("rollback $SETUP")
        LocalData.lastTimeDiagnosisKeysFromServerFetch(lastFetchDateForRollback.get())
    }

    private fun rollbackToken() {
        Timber.v("rollback $TOKEN")
        LocalData.googleApiToken(googleAPITokenForRollback.get())
    }

    private suspend fun rollbackFilesFromWebRequests() {
        Timber.v("rollback $FILES_FROM_WEB_REQUESTS")
        KeyCacheRepository.getDateRepository(CoronaWarnApplication.getAppContext())
            .clear()
    }

    /**
     * Executes the INIT Transaction State
     */
    private suspend fun executeSetup() = executeState(SETUP) {
        lastFetchDateForRollback.set(LocalData.lastTimeDiagnosisKeysFromServerFetch())
        val currentDate = Date(System.currentTimeMillis())
        Timber.d("using $currentDate as current date in Transaction.")
        currentDate
    }

    /**
     * Executes the TOKEN Transaction State
     */
    private suspend fun executeToken() = executeState(TOKEN) {
        googleAPITokenForRollback.set(LocalData.googleApiToken())
        val tempToken = UUID.randomUUID().toString()
        LocalData.googleApiToken(tempToken)
        return@executeState tempToken
    }

    /**
     * Executes the RETRIEVE_RISK_CORE_PARAMS Transaction State
     */
    private suspend fun executeRetrieveRiskScoreParams() =
        executeState(RETRIEVE_RISK_SCORE_PARAMS) {
            ApplicationConfigurationService.asyncRetrieveExposureConfiguration()
        }

    /**
     * Executes the WEB_REQUESTS Transaction State
     */
    private suspend fun executeFetchKeyFilesFromServer(
        currentDate: Date
    ) = executeState(FILES_FROM_WEB_REQUESTS) {
        FileStorageHelper.initializeExportSubDirectory()
        CachedKeyFileHolder.asyncFetchFiles(currentDate)
    }

    /**
     * Executes the API_SUBMISSION Transaction State
     *
     * We currently use Batch Size 1 and thus submit multiple times to the API.
     * This means that instead of directly submitting all files at once, we have to split up
     * our file list as this equals a different batch for Google every time.
     */
    private suspend fun executeAPISubmission(
        token: String,
        exportFiles: Collection<File>,
        exposureConfiguration: ExposureConfiguration?
    ) = executeState(API_SUBMISSION) {
        exportFiles.forEach { batch ->
            InternalExposureNotificationClient.asyncProvideDiagnosisKeys(
                listOf(batch),
                exposureConfiguration,
                token
            )
        }
        Timber.d("Diagnosis Keys provided successfully, Token: $token")
    }

    /**
     * Executes the FETCH_DATE_UPDATE Transaction State
     */
    private suspend fun executeFetchDateUpdate(
        currentDate: Date
    ) = executeState(FETCH_DATE_UPDATE) {
        LocalData.lastTimeDiagnosisKeysFromServerFetch(currentDate)
    }

    /**
     * Executes the CLOSE Transaction State
     */
    private suspend fun executeClose() = executeState(CLOSE) {
        exportFilesForRollback.set(null)
        lastFetchDateForRollback.set(null)
        googleAPITokenForRollback.set(null)
    }
}
