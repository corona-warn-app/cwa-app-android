package de.rki.coronawarnapp.transaction

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.toNewConfig
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.ApplicationConfiguration
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.submission.DefaultKeyConverter
import de.rki.coronawarnapp.submission.ExposureKeyHistoryCalculations
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.TransmissionRiskVectorDeterminator
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.CLOSE
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.RETRIEVE_TAN_AND_SUBMIT_KEYS
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.STORE_SUCCESS
import de.rki.coronawarnapp.util.di.AppInjector
import timber.log.Timber

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

    private const val FALLBACK_COUNTRY = "DE"
    override val TAG: String? = SubmitDiagnosisKeysTransaction::class.simpleName

    /** possible transaction states */
    private enum class SubmitDiagnosisKeysTransactionState :
        TransactionState {
        RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY,
        RETRIEVE_TAN_AND_SUBMIT_KEYS,
        STORE_SUCCESS,
        CLOSE
    }

    private val transactionScope: TransactionCoroutineScope by lazy {
        AppInjector.component.transSubmitDiagnosisInjection.transactionScope
    }

    private val playbook: Playbook
        get() = AppInjector.component.transSubmitDiagnosisInjection.playbook

    private val appConfigProvider: AppConfigProvider
        get() = AppInjector.component.transSubmitDiagnosisInjection.appConfigProvider

    /** initiates the transaction. This suspend function guarantees a successful transaction once completed. */
    suspend fun start(
        registrationToken: String,
        keys: List<TemporaryExposureKey>,
        symptoms: Symptoms
    ) = lockAndExecute(unique = true, scope = transactionScope) {

        val temporaryExposureKeyList = executeState(RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY) {
            ExposureKeyHistoryCalculations(
                TransmissionRiskVectorDeterminator(),
                DefaultKeyConverter()
            ).transformToKeyHistoryInExternalFormat(keys, symptoms)
        }

        val visistedCountries =
            appConfigProvider.getAppConfig().performSanityChecks().supportedCountriesList

        executeState(RETRIEVE_TAN_AND_SUBMIT_KEYS) {
            val submissionData = Playbook.SubmissionData(
                registrationToken = registrationToken,
                temporaryExposureKeys = temporaryExposureKeyList,
                consentToFederation = true,
                visistedCountries = visistedCountries
            )
            playbook.submission(submissionData)
        }

        executeState(STORE_SUCCESS) {
            SubmissionService.submissionSuccessful()
        }

        executeState(CLOSE) {}
    }

    private fun ApplicationConfiguration.performSanityChecks(): ApplicationConfiguration {
        var sanityChecked = this

        if (sanityChecked.supportedCountriesList.isEmpty()) {
            sanityChecked = sanityChecked.toNewConfig {
                addSupportedCountries(FALLBACK_COUNTRY)
            }
            Timber.w("Country list was empty, corrected: %s", sanityChecked.supportedCountriesList)
        }

        return sanityChecked
    }
}
