package de.rki.coronawarnapp.deadman

import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import dagger.Reusable
import de.rki.coronawarnapp.worker.BackgroundConstants
import org.joda.time.DateTimeConstants
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class DeadmanNotificationWorkBuilder @Inject constructor() {

    /**
     * Build one time work
     */
    fun buildOneTimeWork(delay: Long) =
        OneTimeWorkRequestBuilder<DeadmanNotificationOneTimeWorker>()
            .setInitialDelay(
                delay,
                TimeUnit.MINUTES
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BackgroundConstants.BACKOFF_INITIAL_DELAY,
                TimeUnit.MINUTES
            )
            .build()

    /**
     * Build periodic work
     */
    fun buildPeriodicWork() = PeriodicWorkRequestBuilder<DeadmanNotificationPeriodicWorker>(
        DateTimeConstants.HOURS_PER_DAY.toLong(), TimeUnit.MINUTES
    )
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            BackgroundConstants.BACKOFF_INITIAL_DELAY,
            TimeUnit.MINUTES
        )
        .build()
}
