package de.rki.coronawarnapp.presencetracing.storage.retention

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.Reusable
import org.joda.time.DateTimeConstants
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class TraceLocationDbCleanUpScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    private val periodicWorkRequest = PeriodicWorkRequestBuilder<TraceLocationDbCleanUpPeriodicWorker>(
        DateTimeConstants.HOURS_PER_DAY.toLong(),
        TimeUnit.HOURS
    ).build()

    fun scheduleDaily() {
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "TraceLocationCleanUpPeriodicWork"
    }
}
