package de.rki.coronawarnapp.ccl.configuration.update

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
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

        // update is needed if the last update was on a different day
        return lastExecution.toLocalDateUtc() != now.toLocalDateUtc()
    }
}
