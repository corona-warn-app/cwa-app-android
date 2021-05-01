package de.rki.coronawarnapp.coronatest.type.common

import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.rki.coronawarnapp.util.coroutine.await
import java.util.concurrent.TimeUnit

open class ResultScheduler(
    private val workManager: WorkManager,
) {

    internal suspend fun isScheduled(workerName: String) =
        workManager.getWorkInfosForUniqueWork(workerName)
            .await()
            .any { it.isScheduled }

    internal val WorkInfo.isScheduled: Boolean
        get() = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED

    companion object {
        /**
         * Kind initial delay in minutes for periodic work for accessibility reason
         *
         * @see TimeUnit.SECONDS
         */
        internal const val TEST_RESULT_PERIODIC_INITIAL_DELAY = 10L
    }
}
