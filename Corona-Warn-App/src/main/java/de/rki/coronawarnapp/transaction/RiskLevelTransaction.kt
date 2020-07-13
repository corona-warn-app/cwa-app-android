package de.rki.coronawarnapp.transaction

import androidx.core.app.NotificationCompat
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.exception.RiskLevelCalculationException
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevel.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskLevel.LOW_LEVEL_RISK
import de.rki.coronawarnapp.risk.RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF
import de.rki.coronawarnapp.risk.RiskLevel.UNDETERMINED
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_INITIAL
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
import de.rki.coronawarnapp.risk.RiskLevelCalculation
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.transaction.RiskLevelTransaction.RiskLevelTransactionState.CHECK_APP_CONNECTIVITY
import de.rki.coronawarnapp.transaction.RiskLevelTransaction.RiskLevelTransactionState.CHECK_INCREASED_RISK
import de.rki.coronawarnapp.transaction.RiskLevelTransaction.RiskLevelTransactionState.CHECK_TRACING
import de.rki.coronawarnapp.transaction.RiskLevelTransaction.RiskLevelTransactionState.CHECK_UNKNOWN_RISK_INITIAL_NO_KEYS
import de.rki.coronawarnapp.transaction.RiskLevelTransaction.RiskLevelTransactionState.CHECK_UNKNOWN_RISK_INITIAL_TRACING_DURATION
import de.rki.coronawarnapp.transaction.RiskLevelTransaction.RiskLevelTransactionState.CHECK_UNKNOWN_RISK_OUTDATED
import de.rki.coronawarnapp.transaction.RiskLevelTransaction.RiskLevelTransactionState.CLOSE
import de.rki.coronawarnapp.transaction.RiskLevelTransaction.RiskLevelTransactionState.RETRIEVE_APPLICATION_CONFIG
import de.rki.coronawarnapp.transaction.RiskLevelTransaction.RiskLevelTransactionState.RETRIEVE_EXPOSURE_SUMMARY
import de.rki.coronawarnapp.transaction.RiskLevelTransaction.RiskLevelTransactionState.RISK_CALCULATION_DATE_UPDATE
import de.rki.coronawarnapp.transaction.RiskLevelTransaction.RiskLevelTransactionState.UPDATE_RISK_LEVEL
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.millisecondsToHours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

/**
 * The [RiskLevelTransaction] is used to define an atomic Transaction for the Risk Level Calculation.
 * This transaction calculates different Risk Level Scores based on dynamic values and defined constants.
 *
 * In the different states the conditions for all risk level will be checked and if a risk level is found
 * the value will be persisted. Please have a look at the [de.rki.coronawarnapp.risk.RiskLevel] enum class
 * to see all available technical risk level. At the start of the Transaction the risk level is defined
 * as [de.rki.coronawarnapp.risk.RiskLevel.UNDETERMINED]. The different execution states will define the correct
 * risk level and execute the [UPDATE_RISK_LEVEL] & [CLOSE] block to exit the Transaction and
 * prevent further manipulating of the risk level.
 *
 * There is currently a simple rollback behavior defined for this transaction. This means that the last calculated
 * risk level before the transaction started will be set if there is an error.
 *
 * The logic for the risk level calculation is the following:
 * We have 5 different technical risk levels:
 * 1. [NO_CALCULATION_POSSIBLE_TRACING_OFF]
 * 2. [UNKNOWN_RISK_OUTDATED_RESULTS]
 * 3. [INCREASED_RISK]
 * 4. [UNKNOWN_RISK_INITIAL]
 * 5. [LOW_LEVEL_RISK]
 *
 * HIERARCHY EXPLANATION
 *
 * [NO_CALCULATION_POSSIBLE_TRACING_OFF] will always be displayed no matter if a condition of a different risk level
 * is also applicable. It will overrule all other risk levels.
 *
 * [UNKNOWN_RISK_OUTDATED_RESULTS] will overrule [INCREASED_RISK] and [LOW_LEVEL_RISK].
 * This will ensure that the user has the most recent diagnosis keys available.
 *
 * [INCREASED_RISK] will overrule [UNKNOWN_RISK_INITIAL] and [LOW_LEVEL_RISK].
 *
 * [UNKNOWN_RISK_INITIAL] will overrule [LOW_LEVEL_RISK] and [UNKNOWN_RISK_OUTDATED_RESULTS].
 *
 *
 * RISK LEVEL CALCULATION
 *
 * [NO_CALCULATION_POSSIBLE_TRACING_OFF] is calculated if the tracing is disabled at the moment. This will be determined
 * by the current state of the Exposure Notification API (isEnabled function call).
 *
 * [UNKNOWN_RISK_OUTDATED_RESULTS] is calculated if the [RetrieveDiagnosisKeysTransaction] was executed more
 * than [de.rki.coronawarnapp.risk.TimeVariables.MAX_STALE_EXPOSURE_RISK_RANGE] time ago and new diagnosis keys are
 * needed from the backend to calculate an accurate risk level.
 *
 * [INCREASED_RISK] is calculated if the Google Exposure Notification API has provided a valid Exposure Summary and
 * the maximumRiskScore of this summary exceeds the defined threshold that will be retrieved from the
 * backend. The backend call is necessary so that experts can adapt the thresholds in sync with the current
 * development of the pandemic without the need to update the app. This ensures that every app will use
 * the current and correct threshold to calculate the risk level.
 *
 * [UNKNOWN_RISK_INITIAL] is calculated if the user has not retrieved any diagnosis keys from the server (the
 * [RetrieveDiagnosisKeysTransaction] was never executed) and a calculation therefore is simply not possible
 * because the Google Exposure Notification Framework needs those Keys to generate a match.
 * This risk level is also shown to the user if there was a previous Diagnosis Keys Retrieval but the initial
 * tracing activation started less than [de.rki.coronawarnapp.risk.TimeVariables.MIN_ACTIVATED_TRACING_TIME] ago.
 *
 * [LOW_LEVEL_RISK] is applied if the conditions of the above are not met.
 *
 *
 * TRANSACTION EXECUTION
 *
 * The Transaction undergoes multiple States:
 * 1. [CHECK_TRACING]
 * 2. [CHECK_UNKNOWN_RISK_INITIAL_NO_KEYS]
 * 3. [CHECK_UNKNOWN_RISK_OUTDATED]
 * 4. [CHECK_APP_CONNECTIVITY]
 * 5. [RETRIEVE_APPLICATION_CONFIG]
 * 6. [RETRIEVE_EXPOSURE_SUMMARY]
 * 7. [CHECK_INCREASED_RISK]
 * 8. [CHECK_UNKNOWN_RISK_INITIAL_TRACING_DURATION]
 * 9. [UPDATE_RISK_LEVEL]
 * 10. [RISK_CALCULATION_DATE_UPDATE]
 * 11. [CLOSE]
 *
 * This transaction will queue up any start calls and executes them in the given order (unlike the other defined
 * transactions (e.g. [RetrieveDiagnosisKeysTransaction]). This is necessary in order to respond to various trigger
 * events that can occur at any time and will change the previously calculated Risk Level Score.
 *
 * @see Transaction
 *
 * @throws de.rki.coronawarnapp.exception.TransactionException An Exception thrown when an error occurs during Transaction Execution
 * @throws de.rki.coronawarnapp.exception.RollbackException An Exception thrown when an error occurs during Rollback of the Transaction
 */
object RiskLevelTransaction : Transaction() {

    override val TAG: String? = RiskLevelTransaction::class.simpleName

    /** possible transaction states */
    private enum class RiskLevelTransactionState : TransactionState {
        /** Check the conditions for the [NO_CALCULATION_POSSIBLE_TRACING_OFF] score */
        CHECK_TRACING,

        /** Check if the user has started tracing and if he has executed the
         *  [RetrieveDiagnosisKeysTransaction] at least once */
        CHECK_UNKNOWN_RISK_INITIAL_NO_KEYS,

        /** Check the conditions for the [UNKNOWN_RISK_OUTDATED_RESULTS] score */
        CHECK_UNKNOWN_RISK_OUTDATED,

        /** Check if the current app has internet, if not, use the last successful  */
        CHECK_APP_CONNECTIVITY,

        /** Retrieve the Application Configuration values to calculate the Risk Score
         * and determine the [INCREASED_RISK] and [LOW_LEVEL_RISK] */
        RETRIEVE_APPLICATION_CONFIG,

        /** Retrieve the last persisted [ExposureSummary] (if available) from the Google Exposure Notification API for
         * further calculation of the Risk Level Score */
        RETRIEVE_EXPOSURE_SUMMARY,

        /** Check the conditions for the [INCREASED_RISK] score */
        CHECK_INCREASED_RISK,

        /** Check the conditions for the [UNKNOWN_RISK_INITIAL] score */
        CHECK_UNKNOWN_RISK_INITIAL_TRACING_DURATION,

        /** Update and persist the Risk Level Score with the calculated score */
        UPDATE_RISK_LEVEL,

        /** Update of the Date to reflect a complete Transaction State */
        RISK_CALCULATION_DATE_UPDATE,

        /** Transaction Closure */
        CLOSE
    }

    /** atomic reference for the rollback value for the last calculated risk level score */
    private val lastCalculatedRiskLevelScoreForRollback = AtomicReference<RiskLevel>()

    /** atomic reference for the rollback value for date of last risk level calculation */
    private val lastCalculatedRiskLevelDate = AtomicReference<Long>()

    /** initiates the transaction. This suspend function guarantees a successful transaction once completed. */
    suspend fun start() = lockAndExecute {
        /****************************************************
         * CHECK [NO_CALCULATION_POSSIBLE_TRACING_OFF] CONDITIONS
         ****************************************************/
        var result = executeCheckTracing()
        if (isValidResult(result)) return@lockAndExecute

        /****************************************************
         * CHECK [UNKNOWN_RISK_INITIAL] FOR
         * INIT STATE CONDITIONS
         ****************************************************/
        result = executeCheckUnknownRiskInitialNoKeys()
        if (isValidResult(result)) return@lockAndExecute

        /****************************************************
         * CHECK [UNKNOWN_RISK_OUTDATED_RESULTS] CONDITIONS
         ****************************************************/
        result = executeCheckUnknownRiskOutdatedResults()
        if (isValidResult(result)) return@lockAndExecute

        /****************************************************
         * [CHECK_APP_CONNECTIVITY]
         ****************************************************/
        if (!executeCheckAppConnectivity()) {
            executeClose()
            return@lockAndExecute
        }

        /****************************************************
         * RETRIEVE APPLICATION CONFIGURATION
         ****************************************************/
        val appConfiguration = executeRetrieveApplicationConfiguration()

        /****************************************************
         * RETRIEVE EXPOSURE SUMMARY
         ****************************************************/
        val lastExposureSummary = executeRetrieveExposureSummary()

        /****************************************************
         * CHECK [INCREASED_RISK] CONDITIONS
         ****************************************************/
        result = executeCheckIncreasedRisk(appConfiguration, lastExposureSummary)
        if (isValidResult(result)) return@lockAndExecute

        /****************************************************
         * CHECK [UNKNOWN_RISK_INITIAL] FOR TRACING
         * DURATION THRESHOLD
         ****************************************************/
        result = executeCheckUnknownRiskInitialTracingDuration()
        if (isValidResult(result)) return@lockAndExecute

        /****************************************************
         * SET [LOW_LEVEL_RISK] LEVEL IF NONE ABOVE APPLIED
         ****************************************************/
        if (result == UNDETERMINED) {
            lastCalculatedRiskLevelScoreForRollback.set(RiskLevelRepository.getLastCalculatedScore())
            executeUpdateRiskLevelScore(LOW_LEVEL_RISK)
            executeRiskLevelCalculationDateUpdate()
            executeClose()
            return@lockAndExecute
        } else {
            throw RiskLevelCalculationException(IllegalStateException("no risk level could be determined"))
        }
    }

    override suspend fun rollback() {
        super.rollback()
        try {
            if (UPDATE_RISK_LEVEL.isInStateStack()) {
                updateRiskLevelScore(lastCalculatedRiskLevelScoreForRollback.get())
            }
            if (RISK_CALCULATION_DATE_UPDATE.isInStateStack()) {
                LocalData.lastTimeRiskLevelCalculation(lastCalculatedRiskLevelDate.get())
            }
        } catch (e: Exception) {
            // We handle every exception through a RollbackException to make sure that a single EntryPoint
            // is available for the caller.
            handleRollbackError(e)
        }
    }

    /**
     * Executes the [CHECK_TRACING] Transaction State
     */
    private suspend fun executeCheckTracing(): RiskLevel = executeState(CHECK_TRACING) {
        // this applies if tracing is not activated
        val isTracingEnabled = InternalExposureNotificationClient.asyncIsEnabled()
        if (!isTracingEnabled) return@executeState NO_CALCULATION_POSSIBLE_TRACING_OFF

        Timber.v("$transactionId - TRACING_NOT_ACTIVE_RISK not applicable")
        return@executeState UNDETERMINED
    }

    /**
     * Executes the [CHECK_UNKNOWN_RISK_INITIAL_NO_KEYS] Transaction State
     */
    private suspend fun executeCheckUnknownRiskInitialNoKeys(): RiskLevel = executeState(
        CHECK_UNKNOWN_RISK_INITIAL_NO_KEYS
    ) {
        // if there was no key retrieval before, we return no calculation state
        TimeVariables.getLastTimeDiagnosisKeysFromServerFetch()
            ?: return@executeState UNKNOWN_RISK_INITIAL.also {
                Timber.v("$transactionId - no last time diagnosis keys from server fetch timestamp was found")
            }

        Timber.v("$transactionId - CHECK_UNKNOWN_RISK_INITIAL_NO_KEYS not applicable")
        return@executeState UNDETERMINED
    }

    /**
     * Executes the [CHECK_UNKNOWN_RISK_OUTDATED] Transaction State
     */
    private suspend fun executeCheckUnknownRiskOutdatedResults(): RiskLevel =
        executeState(CHECK_UNKNOWN_RISK_OUTDATED) {

            // if the last calculation is longer in the past as the defined threshold we return the stale state
            val timeSinceLastDiagnosisKeyFetchFromServer =
                TimeVariables.getTimeSinceLastDiagnosisKeyFetchFromServer()
                    ?: throw RiskLevelCalculationException(
                        IllegalArgumentException("time since last exposure calculation is null")
                    )

            /** we only return outdated risk level if the threshold is reached AND the active tracing time is above the
            defined threshold because [UNKNOWN_RISK_INITIAL] overrules [UNKNOWN_RISK_OUTDATED_RESULTS] */
            if (timeSinceLastDiagnosisKeyFetchFromServer.millisecondsToHours() >
                TimeVariables.getMaxStaleExposureRiskRange() && isActiveTracingTimeAboveThreshold()
            ) {
                if (ConnectivityHelper.isBackgroundJobEnabled(CoronaWarnApplication.getAppContext())) {
                    return@executeState UNKNOWN_RISK_OUTDATED_RESULTS.also {
                        Timber.v("diagnosis keys outdated and active tracing time is above threshold")
                        Timber.v("manual mode not active (background jobs enabled)")
                    }
                } else {
                    return@executeState UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL.also {
                        Timber.v("diagnosis keys outdated and active tracing time is above threshold")
                        Timber.v("manual mode active (background jobs disabled)")
                    }
                }
            }

            Timber.v("$transactionId - CHECK_UNKNOWN_RISK_OUTDATED not applicable")
            return@executeState UNDETERMINED
        }

    /**
     * Executes the [CHECK_APP_CONNECTIVITY] Transaction State
     * If there is no connectivity the transaction will set the last calculated
     * risk level and closes the transaction
     *
     * @return
     */
    private suspend fun executeCheckAppConnectivity(): Boolean =
        executeState(CHECK_APP_CONNECTIVITY) {
            val isNetworkEnabled =
                ConnectivityHelper.isNetworkEnabled(CoronaWarnApplication.getAppContext())

            if (!isNetworkEnabled) {
                RiskLevelRepository.setLastCalculatedRiskLevelAsCurrent()
                return@executeState false
            }
            return@executeState true
        }

    /**
     * Executes the [RETRIEVE_APPLICATION_CONFIG] Transaction State
     *
     * @return the values of the application configuration
     */
    private suspend fun executeRetrieveApplicationConfiguration():
            ApplicationConfigurationOuterClass.ApplicationConfiguration =
        executeState(RETRIEVE_APPLICATION_CONFIG) {
            return@executeState getApplicationConfiguration()
                .also {
                    Timber.v(TAG, "$transactionId - retrieved configuration from backend")
                }
        }

    /**
     * Executes the [RETRIEVE_EXPOSURE_SUMMARY] Transaction State
     */
    private suspend fun executeRetrieveExposureSummary(): ExposureSummary =
        executeState(RETRIEVE_EXPOSURE_SUMMARY) {
            val lastExposureSummary = getLastExposureSummary() ?: getNewExposureSummary()

            return@executeState lastExposureSummary.also {
                Timber.v(TAG, "$transactionId - get the exposure summary for further calculation")
            }
        }

    /**
     * Executes the [CHECK_INCREASED_RISK] Transaction State
     */
    private suspend fun executeCheckIncreasedRisk(
        appConfig: ApplicationConfigurationOuterClass.ApplicationConfiguration,
        exposureSummary: ExposureSummary
    ): RiskLevel =
        executeState(CHECK_INCREASED_RISK) {

            // custom attenuation parameters to weight the attenuation
            // values provided by the Google API
            val attenuationParameters = appConfig.attenuationDuration

            // calculate the risk score based on the values collected by the Google EN API and
            // the backend configuration
            val riskScore = RiskLevelCalculation.calculateRiskScore(
                attenuationParameters,
                exposureSummary
            ).also {
                Timber.v(TAG, "calculated risk with the given config: $it")
            }

            // these are the defined risk classes. They will divide the calculated
            // risk score into the low and increased risk
            val riskScoreClassification = appConfig.riskScoreClasses

            // get the high risk score class
            val highRiskScoreClass =
                riskScoreClassification.riskClassesList.find { it.label == "HIGH" }
                    ?: throw RiskLevelCalculationException(IllegalStateException("no high risk score class found"))

            // if the calculated risk score is above the defined level threshold we return the high level risk score
            if (riskScore >= highRiskScoreClass.min && riskScore <= highRiskScoreClass.max) {
                Timber.v("$riskScore is above the defined min value ${highRiskScoreClass.min}")
                return@executeState INCREASED_RISK
            } else if (riskScore > highRiskScoreClass.max) {
                throw RiskLevelCalculationException(
                    IllegalStateException("risk score is above the max threshold for score class")
                )
            }

            Timber.v("$transactionId - INCREASED_RISK not applicable")
            return@executeState UNDETERMINED
        }

    /**
     * Executes the [CHECK_UNKNOWN_RISK_INITIAL_TRACING_DURATION] Transaction State
     */
    private suspend fun executeCheckUnknownRiskInitialTracingDuration() = executeState(
        CHECK_UNKNOWN_RISK_INITIAL_TRACING_DURATION
    ) {
        // if the active tracing duration is not above the defined threshold we return no calculation state
        if (!isActiveTracingTimeAboveThreshold()) {
            Timber.v("$transactionId - active tracing time is not enough")
            return@executeState UNKNOWN_RISK_INITIAL
        }

        Timber.v("$transactionId - UNKNOWN_RISK_INITIAL not applicable")
        return@executeState UNDETERMINED
    }

    /**
     * Executes the [UPDATE_RISK_LEVEL] Transaction State
     */
    private suspend fun executeUpdateRiskLevelScore(riskLevel: RiskLevel) =
        executeState(UPDATE_RISK_LEVEL) {
            Timber.v("$transactionId - update the risk level with $riskLevel")
            updateRiskLevelScore(riskLevel)
        }

    /**
     * Executes the [CLOSE] Transaction State
     */
    private suspend fun executeClose() = executeState(CLOSE) {
        Timber.v("$transactionId - transaction will close")
        lastCalculatedRiskLevelScoreForRollback.set(null)
        lastCalculatedRiskLevelDate.set(null)
    }

    /****************************************************
     * HELPER AND GETTER FUNCTIONS
     ****************************************************/

    /**
     * Checks if the result of an executed state returns a valid Risk Level Score value.
     * If that is the case the [RiskLevelTransaction] can update the Risk Level Score with the calculated
     * value and close the transaction
     *
     * @param riskLevel calculated Risk Level Score
     * @return if a valid score was already found
     */
    private suspend fun isValidResult(riskLevel: RiskLevel): Boolean {
        if (riskLevel != UNDETERMINED) {
            Timber.v("$transactionId - $riskLevel was determined by the transaction. UPDATE and CLOSE will be called")
            lastCalculatedRiskLevelScoreForRollback.set(RiskLevelRepository.getLastCalculatedScore())
            executeUpdateRiskLevelScore(riskLevel)
            lastCalculatedRiskLevelDate.set(LocalData.lastTimeRiskLevelCalculation())
            executeRiskLevelCalculationDateUpdate()
            executeClose()
            return true
        }
        return false
    }

    /**
     * Returns the last stored ExposureSummary from the storage.
     * The ExposureSummary will be updated in the [de.rki.coronawarnapp.receiver.ExposureStateUpdateReceiver]
     * once the BroadcastReceiver is triggered from the Google Exposure Notification API
     *
     * @return exposure summary from Google Exposure Notification API
     */
    private suspend fun getLastExposureSummary(): ExposureSummary? {
        return ExposureSummaryRepository.getExposureSummaryRepository()
            .getLatestExposureSummary().also {
                Timber.v("used exposure summary for the risk level calculation: $it")
            }
    }

    /**
     * Make a call to the backend to retrieve the current application configuration values
     *
     * @return the [ApplicationConfigurationOuterClass.ApplicationConfiguration] from the backend
     */
    private suspend fun getApplicationConfiguration(): ApplicationConfigurationOuterClass.ApplicationConfiguration =
        withContext(Dispatchers.Default) {
            return@withContext ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()
                .also { Timber.v("configuration from backend: $it") }
        }

    /**
     * Returns a Boolean if the duration of the activated tracing time is above the
     * defined value
     *
     * @return Boolean if threshold is reached
     */
    private fun isActiveTracingTimeAboveThreshold(): Boolean {
        val durationTracingIsActive = TimeVariables.getTimeActiveTracingDuration()
        val durationTracingIsActiveThreshold = TimeVariables.getMinActivatedTracingTime().toLong()

        val activeTracingDurationInHours = durationTracingIsActive.millisecondsToHours()

        return (activeTracingDurationInHours >= durationTracingIsActiveThreshold).also {
            Timber.v(
                "active tracing time ($activeTracingDurationInHours h) is above threshold " +
                        "($durationTracingIsActiveThreshold h): $it"
            )
        }
    }

    /**
     * Updates the Risk Level Score in the repository with the calculated Risk Level
     *
     * @param riskLevel
     */
    private fun updateRiskLevelScore(riskLevel: RiskLevel) {
        val lastCalculatedScore = RiskLevelRepository.getLastCalculatedScore()
        if (RiskLevel.riskLevelChangedBetweenLowAndHigh(lastCalculatedScore, riskLevel)) {
            NotificationHelper.sendNotification(
                CoronaWarnApplication.getAppContext().getString(R.string.notification_body),
                NotificationCompat.PRIORITY_HIGH
            )
        }
        RiskLevelRepository.setRiskLevelScore(riskLevel)
    }

    /**
     * If there is no persisted exposure summary we try to get a new one with the last persisted
     * Google API token that was used in the [de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction]
     *
     * @return a exposure summary from the Google Exposure Notification API
     */
    private suspend fun getNewExposureSummary(): ExposureSummary {
        val googleToken = LocalData.googleApiToken()
            ?: throw RiskLevelCalculationException(IllegalStateException("exposure summary is not persisted"))

        val exposureSummary =
            InternalExposureNotificationClient.asyncGetExposureSummary(googleToken)

        ExposureSummaryRepository.getExposureSummaryRepository()
            .insertExposureSummaryEntity(exposureSummary)

        return exposureSummary.also {
            Timber.v("$transactionId - generated new exposure summary with $googleToken")
        }
    }

    /**
     * Executes the CALCULATION_DATE_UPDATE Transaction State
     */
    private suspend fun executeRiskLevelCalculationDateUpdate() {
        val currentDate = System.currentTimeMillis()
        executeState(RISK_CALCULATION_DATE_UPDATE) {
            LocalData.lastTimeRiskLevelCalculation(currentDate)
        }
    }
}
