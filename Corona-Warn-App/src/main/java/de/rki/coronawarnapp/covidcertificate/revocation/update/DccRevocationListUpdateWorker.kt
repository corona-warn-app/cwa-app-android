package de.rki.coronawarnapp.covidcertificate.revocation.update

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class DccRevocationListUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val dccRevocationListUpdater: DccRevocationListUpdater
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("RevocationListUpdateWorker - Started")
        dccRevocationListUpdater.updateRevocationList(false)
        Timber.d("RevocationListUpdateWorker - Finished")
        return Result.success()
    }
}
