package de.rki.coronawarnapp.covidcertificate.revocation.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class RevocationUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val revocationUpdater: RevocationUpdater
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // satisfy CI ¯\_(ツ)_/¯
        Timber.e("Not yet implemented")
        return Result.failure()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<RevocationUpdateWorker>
}
