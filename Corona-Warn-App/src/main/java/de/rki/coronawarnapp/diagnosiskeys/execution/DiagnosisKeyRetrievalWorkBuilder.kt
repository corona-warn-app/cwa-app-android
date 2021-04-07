package de.rki.coronawarnapp.diagnosiskeys.execution

import androidx.work.BackoffPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import dagger.Reusable
import de.rki.coronawarnapp.worker.BackgroundConstants
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class DiagnosisKeyRetrievalWorkBuilder @Inject constructor() {

    /**
     * This has no network constraints, because even if there is no internet,
     * the worker+task will trigger diagnosis key submission to the ENF.
     * We don't want to prevent that.
     */
    fun createPeriodicWorkRequest(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<DiagnosisKeyRetrievalWorker>(
            60,
            TimeUnit.MINUTES
        )
            .setInitialDelay(
                BackgroundConstants.KIND_DELAY,
                TimeUnit.MINUTES
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BackgroundConstants.BACKOFF_INITIAL_DELAY,
                TimeUnit.MINUTES
            )
            .build()
}
