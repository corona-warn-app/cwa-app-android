package de.rki.coronawarnapp.ccl.configuration.update

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.repositories.UpdateResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CCLConfigurationUpdater @Inject constructor(
    private val timeStamper: TimeStamper,
    private val cclSettings: CCLSettings,
    private val boosterRulesRepository: BoosterRulesRepository,
    private val cclConfigurationRepository: CCLConfigurationRepository,
    private val dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger
) {

    suspend fun updateIfRequired() {
        Timber.d("update()")

        if (!isUpdateRequired()) {
            Timber.d("No CCLConfig update required!")
            return
        }

        updateAndTriggerRecalculation()
    }

    /**
     * updates the configuration irrespectively of whether it was recently updated
     */
    suspend fun forceUpdate() {
        updateAndTriggerRecalculation()
    }

    private suspend fun updateAndTriggerRecalculation() {
        val updated = updateConfiguration()
        dccWalletInfoUpdateTrigger.triggerDccWalletInfoUpdateAfterConfigUpdate(updated)
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
            val cclConfigDeferred = async { cclConfigurationRepository.updateCCLConfiguration() }

            val boosterRulesResult = boosterRulesDeferred.await()
            val cclConfigResult = cclConfigDeferred.await()

            updateExecutionTimeOnSuccess(boosterRulesResult, cclConfigResult)

            val newBoosterRules = (boosterRulesResult == UpdateResult.UPDATE)
            val newCclConfig = (cclConfigResult == UpdateResult.UPDATE)

            return@coroutineScope newBoosterRules || newCclConfig
        }
    }

    private fun updateExecutionTimeOnSuccess(
        boosterRulesUpdateResult: UpdateResult,
        cclConfigUpdateResult: UpdateResult
    ) {
        if (boosterRulesUpdateResult != UpdateResult.FAIL && cclConfigUpdateResult != UpdateResult.FAIL) {
            cclSettings.setExecutionTimeToNow()
        }
    }
}
