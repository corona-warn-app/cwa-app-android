package de.rki.coronawarnapp.reyclebin.cleanup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class RecycleBinCleanUpWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val recycleBinCleanUpService: RecycleBinCleanUpService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = try {
        Timber.d("Starting recycle bin clean up")
        recycleBinCleanUpService.clearRecycledCertificates()

        Result.success()
    } catch (e: Exception) {
        Timber.e(e, "Recycle bin clean up failed")
        Result.retry()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<RecycleBinCleanUpWorker>
}
