package de.rki.coronawarnapp.deadman

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
class DeadmanNotificationWorkBuilder @Inject constructor() {

    fun buildOneTimeWork(delay: Long): OneTimeWorkRequest =
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

    fun buildPeriodicWork(): PeriodicWorkRequest = PeriodicWorkRequestBuilder<DeadmanNotificationPeriodicWorker>(
        DateTimeConstants.MINUTES_PER_HOUR.toLong(), TimeUnit.MINUTES
    )
        .setInitialDelay(
            BackgroundConstants.KIND_DELAY,
            TimeUnit.MINUTES
        )
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            BackgroundConstants.BACKOFF_INITIAL_DELAY,
            TimeUnit.MINUTES
        )
        .build()
}
