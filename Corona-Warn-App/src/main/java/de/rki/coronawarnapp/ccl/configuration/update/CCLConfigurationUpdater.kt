package de.rki.coronawarnapp.ccl.configuration.update

import androidx.annotation.VisibleForTesting
import org.joda.time.Duration
import org.joda.time.Instant
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun isUpdateRequired(now: Instant = Instant.now()): Boolean {
        val lastExecution = cclSettings.getLastExecutionTime() ?: return true
        val minTimeOfNextUpdate = lastExecution.plus(updateInterval)
        return now.isAfter(minTimeOfNextUpdate)
    }

    companion object {

        // CCL Config should be updated daily
        private val updateInterval = Duration.standardDays(1)
    }
}
