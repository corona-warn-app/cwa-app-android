package de.rki.coronawarnapp.covidcertificate.common.statecheck

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.expiration.DccValidityStateNotificationService
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class DccStateCheckWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dccValidityStateNotificationService: DccValidityStateNotificationService,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = try {
        dccValidityStateNotificationService.showNotificationIfStateChanged()

        Result.success()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "DCC state check failed.")
        Result.retry()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<DccStateCheckWorker>

    companion object {
        private val TAG = DccStateCheckWorker::class.java.simpleName
    }
}
