package de.rki.coronawarnapp.datadonation.analytics.worker

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import dagger.Reusable
import de.rki.coronawarnapp.worker.BackgroundConstants
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class DataDonationAnalyticsWorkBuilder @Inject constructor() {
    fun buildPeriodicWork(initialDelay: Duration): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<DataDonationAnalyticsPeriodicWorker>(Duration.ofDays(1))
            .setInitialDelay(
                initialDelay.toHours(),
                TimeUnit.HOURS
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BackgroundConstants.BACKOFF_INITIAL_DELAY,
                TimeUnit.MINUTES
            )
            .setConstraints(buildConstraints())
            .build()

    private fun buildConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
}
