package de.rki.coronawarnapp.covidcertificate.common.statecheck

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.expiration.DccValidityStateNotificationService
import de.rki.coronawarnapp.tag
import timber.log.Timber

@HiltWorker
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

    companion object {
        private val TAG = tag<DccStateCheckWorker>()
    }
}
