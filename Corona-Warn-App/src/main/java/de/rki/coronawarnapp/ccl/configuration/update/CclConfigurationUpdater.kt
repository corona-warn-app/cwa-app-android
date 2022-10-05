package de.rki.coronawarnapp.ccl.configuration.update

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.expiration.DccValidityStateChangeObserver
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.repositories.UpdateResult
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CclConfigurationUpdater @Inject constructor(
    private val timeStamper: TimeStamper,
    private val cclSettings: CclSettings,
    private val boosterRulesRepository: BoosterRulesRepository,
    private val cclConfigurationRepository: CclConfigurationRepository,
    private val dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger,
    private val dccValidationRepository: DccValidationRepository,
    private val dccValidityStateChangeObserver: DccValidityStateChangeObserver
) {

    private val mutex = Mutex()

    suspend fun updateIfRequired() = mutex.withLock {
        Timber.d("update()")
        dccValidityStateChangeObserver.acknowledgeStateOfCertificate()

        if (isUpdateRequired()) {
            Timber.d("CCLConfig update required!")
            updateAndTriggerRecalculation()
        } else {
            Timber.d("No CCLConfig update required!")
            triggerRecalculation(configurationChanged = true)
        }
    }

    /**
     * updates the configuration irrespectively of whether it was recently updated
     */
    suspend fun forceUpdate() {
        updateAndTriggerRecalculation()
    }

    private suspend fun updateAndTriggerRecalculation() {
        val updated = updateConfiguration()
        triggerRecalculation(configurationChanged = updated)
    }

    private suspend fun triggerRecalculation(configurationChanged: Boolean) {
        dccWalletInfoUpdateTrigger.triggerAfterConfigChange(configurationChanged)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun isUpdateRequired(now: Instant = timeStamper.nowUTC): Boolean {
        val lastExecution = cclSettings.getLastExecutionTime() ?: return true

        // update is needed if the last update was on a different day
        return lastExecution.toLocalDateUtc() != now.toLocalDateUtc()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun updateConfiguration(): Boolean {
        return coroutineScope {
            val boosterRulesDeferred = async { boosterRulesRepository.update() }
            val cclConfigDeferred = async { cclConfigurationRepository.updateCclConfiguration() }
            val invalidationRulesDeferred = async { dccValidationRepository.updateInvalidationRules() }

            val updateResults = awaitAll(boosterRulesDeferred, cclConfigDeferred, invalidationRulesDeferred)

            if (updateResults.none { it == UpdateResult.FAIL }) cclSettings.setExecutionTimeToNow()

            return@coroutineScope updateResults.any { it == UpdateResult.UPDATE }
        }
    }
}
