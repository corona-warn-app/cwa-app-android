package de.rki.coronawarnapp.ccl.configuration.update

import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

class CCLConfigurationUpdater @Inject constructor(
    private val cclSettings: CCLSettings
) {

    suspend fun update(): Boolean {
        Timber.d("update()")

        if (!isUpdateRequired()) {
            Timber.d("No CCLConfig update required!")
            return false
        }

        return true
    }

    private suspend fun isUpdateRequired(): Boolean {
        val lastExecution = cclSettings.getLastExecutionTime() ?: return false
        val minTimeOfNextUpdate = lastExecution.plus(updateInterval)
        return minTimeOfNextUpdate.isBeforeNow
    }

    companion object {

        // CCL Config should be updated daily
        private val updateInterval = Duration.standardDays(1)
    }
}
