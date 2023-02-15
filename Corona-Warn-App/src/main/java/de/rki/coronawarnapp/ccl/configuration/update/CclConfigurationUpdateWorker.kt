package de.rki.coronawarnapp.ccl.configuration.update

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class CclConfigurationUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val cclConfigurationUpdater: CclConfigurationUpdater
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("Start updating the ccl configuration.")
        cclConfigurationUpdater.updateIfRequired()
        Timber.d("Finished updating the ccl configuration.")
        return Result.success()
    }
}
