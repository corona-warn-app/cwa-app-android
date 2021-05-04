package de.rki.coronawarnapp.risk.execution

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import timber.log.Timber

abstract class RiskWorkScheduler(
    private val workManager: WorkManager,
    private val logTag: String
) {

    abstract suspend fun isScheduled(): Boolean

    internal abstract fun setPeriodicRiskCalculation(enabled: Boolean)

    internal fun queueWorker(workerId: String, request: PeriodicWorkRequest) {
        Timber.tag(logTag).d("queueWorker(workerId=%s, request=%s)", workerId, request)
        workManager.enqueueUniquePeriodicWork(
            workerId,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    internal fun cancelWorker(workerId: String) {
        Timber.tag(logTag).d("cancelWorker(workerId=$workerId")
        workManager.cancelUniqueWork(workerId)
    }

    internal val WorkInfo.isScheduled: Boolean
        get() = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED
}
