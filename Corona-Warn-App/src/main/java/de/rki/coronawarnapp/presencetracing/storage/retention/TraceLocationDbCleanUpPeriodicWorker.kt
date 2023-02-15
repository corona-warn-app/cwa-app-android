package de.rki.coronawarnapp.presencetracing.storage.retention

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class TraceLocationDbCleanUpPeriodicWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val traceLocationCleaner: TraceLocationCleaner,
    private val checkInCleaner: CheckInCleaner
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("Starting work in TraceLocationDbCleanUpPeriodicWorker")
        try {
            traceLocationCleaner.cleanUp()
            checkInCleaner.cleanUp()
        } catch (exception: Exception) {
            Timber.e(exception, "Work in TraceLocationDbCleanUpPeriodicWorker failed")
            return Result.failure()
        }
        Timber.d("Work in TraceLocationDbCleanUpPeriodicWorker successfully completed!")
        return Result.success()
    }
}
