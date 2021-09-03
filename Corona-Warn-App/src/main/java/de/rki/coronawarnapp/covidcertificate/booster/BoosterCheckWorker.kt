package de.rki.coronawarnapp.covidcertificate.booster

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class BoosterCheckWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val boosterNotificationService: BoosterNotificationService
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result = try {
        Timber.d("Starting booster notification check")
        boosterNotificationService.checkBoosterNotification()

        Result.success()
    } catch (e: Exception) {
        Timber.e(e, "Booster notification check failed")
        Result.retry()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<BoosterCheckWorker>
}
