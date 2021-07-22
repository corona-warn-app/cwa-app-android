package de.rki.coronawarnapp.covidcertificate.signature.core.execution

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationCheck
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory

class DccStateCheckWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dccExpirationCheck: DccExpirationCheck,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        dccExpirationCheck.checkExpirationStates()
        return Result.retry()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<DccStateCheckWorker>

    companion object {
        private val TAG = DccStateCheckWorker::class.java.simpleName
    }
}
