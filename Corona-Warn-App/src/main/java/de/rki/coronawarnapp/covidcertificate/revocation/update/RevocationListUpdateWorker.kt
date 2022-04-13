package de.rki.coronawarnapp.covidcertificate.revocation.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class RevocationListUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val revocationListUpdater: RevocationListUpdater
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("RevocationListUpdateWorker - Started")
        revocationListUpdater.updateRevocationList(false)
        Timber.d("RevocationListUpdateWorker - Finished")
        return Result.success()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<RevocationListUpdateWorker>
}
