package de.rki.coronawarnapp.datadonation.analytics.worker

import dagger.Reusable
import org.joda.time.Duration
import javax.inject.Inject
import kotlin.random.Random

@Reusable
class DataDonationAnalyticsTimeCalculation @Inject constructor() {
    /**
     * Get initial delay in hours for analytics one time submission work
     * currently there is no dependency on context but might appear later
     */
    fun getDelay(): Duration = Duration.standardHours(Random.nextLong(0, 24))
}
