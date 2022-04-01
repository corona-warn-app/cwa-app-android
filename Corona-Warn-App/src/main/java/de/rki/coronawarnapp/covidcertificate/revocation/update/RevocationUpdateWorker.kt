package de.rki.coronawarnapp.covidcertificate.revocation.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory

class RevocationUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val revocationUpdater: RevocationUpdater
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<RevocationUpdateWorker>
}
