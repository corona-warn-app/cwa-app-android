package de.rki.coronawarnapp.ccl.configuration.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class CCLConfigurationUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val cclConfigurationUpdater: CCLConfigurationUpdater
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("Start updating the ccl configuration.")
        cclConfigurationUpdater.updateIfRequired()

        TODO("Should we retry if updateIfRequired() fails?")

        return Result.success()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<CCLConfigurationUpdateWorker>
}
