package de.rki.coronawarnapp.eventregistration.storage.retention

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class EventRegistrationDbCleanUpPeriodicWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val traceLocationCleaner: TraceLocationCleaner
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("Starting work in TraceLocationCleanUpWorker")
        try {
            traceLocationCleaner.cleanUp()
        } catch (exception: Exception) {
            Timber.e("Work in TraceLocationCleanUpWorker failed: $exception")
            return Result.failure()
        }
        Timber.d("Work in TraceLocationCleanUpWorker successfully completed!")
        return Result.success()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<EventRegistrationDbCleanUpPeriodicWorker>
}
