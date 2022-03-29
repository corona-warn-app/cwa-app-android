package de.rki.coronawarnapp.presencetracing.checkins.checkout.auto

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.common.api.ApiException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.presencetracing.checkins.checkout.CheckOutNotification
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class AutoCheckOutWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val autoCheckOut: AutoCheckOut,
    private val checkOutNotification: CheckOutNotification,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = try {
        val targetId = inputData.getLong(ARGKEY_CHECKIN_ID, 0L)
        val hadTargetedCheckout = if (targetId != 0L) {
            Timber.tag(TAG).d("Performing check-out for $targetId")
            autoCheckOut.performCheckOut(targetId)
        } else {
            false
        }

        val isRefresh = inputData.getBoolean(ARGKEY_PROCESS_OVERDUE, false)
        val overDueCheckouts = if (isRefresh) {
            Timber.tag(TAG).d("Refreshing auto check-out alarm.")
            autoCheckOut.processOverDueCheckouts()
        } else {
            emptyList()
        }

        if (targetId == 0L && !isRefresh) {
            IllegalStateException().reportProblem(TAG, "Problematic worker arguments.")
        }

        autoCheckOut.refreshAlarm()

        val checkInIdForNotification = when {
            hadTargetedCheckout -> targetId
            overDueCheckouts.isNotEmpty() -> overDueCheckouts.first()
            else -> null
        }

        Timber.tag(TAG).d("checkInIdForNotification = $checkInIdForNotification")

        checkInIdForNotification?.let {
            checkOutNotification.showAutoCheckoutNotification(it)
        }

        Result.success()
    } catch (e: ApiException) {
        e.reportProblem(TAG, "Failed to perform auto checkout.")
        Result.failure()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<AutoCheckOutWorker>

    companion object {
        const val ARGKEY_CHECKIN_ID = "autoCheckout.checkInId"
        const val ARGKEY_PROCESS_OVERDUE = "autoCheckout.overdue"
        private val TAG = AutoCheckOutWorker::class.java.simpleName
    }
}
