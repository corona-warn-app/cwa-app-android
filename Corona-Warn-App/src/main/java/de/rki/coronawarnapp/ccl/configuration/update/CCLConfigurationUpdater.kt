package de.rki.coronawarnapp.ccl.configuration.update

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
        cclSettings.setExecutionTimeToNow()
    }

    /**
     * updates the configuration irrespectively of whether it was recently updated
     */
    suspend fun forceUpdate() {
        updateAndTriggerRecalculation()
    }

    private suspend fun updateAndTriggerRecalculation() {
        val updated = updateConfiguration()
        dccWalletInfoUpdateTrigger.triggerDccWalletInfoUpdate(updated)
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
            val newBoosterRulesDownloaded = async { boosterRulesRepository.update() }
            val newCclConfigDownloaded = async { cclConfigurationRepository.updateCCLConfiguration() }

            return@coroutineScope listOf(
                newBoosterRulesDownloaded,
                newCclConfigDownloaded
            ).awaitAll().any { downloaded -> downloaded }
        }
    }
}
