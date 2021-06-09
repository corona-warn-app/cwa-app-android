package de.rki.coronawarnapp.covidcertificate.vaccination.core.execution.worker

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import dagger.Reusable
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningWorker
import de.rki.coronawarnapp.worker.BackgroundConstants
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class VaccinationUpdateWorkerRequestBuilder @Inject constructor() {

    fun createPeriodicWorkRequest(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<PresenceTracingWarningWorker>(
            24,
            TimeUnit.HOURS
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
            .setConstraints(buildConstraints())
            .build()

    private fun buildConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
}
