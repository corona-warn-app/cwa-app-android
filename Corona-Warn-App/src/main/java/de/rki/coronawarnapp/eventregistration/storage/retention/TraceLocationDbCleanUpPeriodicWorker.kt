package de.rki.coronawarnapp.eventregistration.storage.retention

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

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
            Timber.e("Work in TraceLocationDbCleanUpPeriodicWorker failed: $exception")
            return Result.failure()
        }
        Timber.d("Work in TraceLocationDbCleanUpPeriodicWorker successfully completed!")
        return Result.success()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<TraceLocationDbCleanUpPeriodicWorker>
}
