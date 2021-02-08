package de.rki.coronawarnapp.datadonation.analytics.worker

import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import dagger.Reusable
import de.rki.coronawarnapp.worker.BackgroundConstants
import org.joda.time.DateTimeConstants
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class DataDonationAnalyticsWorkBuilder @Inject constructor() {
    fun buildPeriodicWork(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<DataDonationAnalyticsPeriodicWorker>(
            DateTimeConstants.HOURS_PER_DAY.toLong(), TimeUnit.HOURS
        )
            .setInitialDelay(
                DateTimeConstants.HOURS_PER_DAY.toLong(),
                TimeUnit.HOURS
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BackgroundConstants.BACKOFF_INITIAL_DELAY,
                TimeUnit.MINUTES
            )
            .build()

    fun buildOneTime(initialDelayInHours: Long): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<DataDonationAnalyticsOneTimeWorker>()
            .setInitialDelay(
                initialDelayInHours,
                TimeUnit.HOURS
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BackgroundConstants.BACKOFF_INITIAL_DELAY,
                TimeUnit.MINUTES
            )
            .build()
}
