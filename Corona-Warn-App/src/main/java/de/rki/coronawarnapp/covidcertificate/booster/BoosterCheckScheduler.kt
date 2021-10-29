package de.rki.coronawarnapp.covidcertificate.booster

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoosterCheckScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val foregroundState: ForegroundState,
    private val workManager: WorkManager,
    private val boosterNotificationService: BoosterNotificationService
) {

    fun setup() {
        Timber.d("setup()")

        foregroundState.isInForeground
            .onStart {
                Timber.v("Monitoring foregroundstate (booster notification check) ")
                // Due to KEEP policy, we just call to make sure it's scheduled
                schedulePeriodicWorker()
            }
            .distinctUntilChanged()
            .filter { it } // Only when going into foreground
            .onEach {
                boosterNotificationService.checkBoosterNotification()
            }
            .launchIn(appScope)
    }

    fun runNow() {
        Timber.d("runNow()")
        foregroundState.isInForeground
            .distinctUntilChanged()
            .filter { it }
            .onEach {
                Timber.v("Run booster rules")
                boosterNotificationService.checkBoosterNotification(forceCheck = true)
            }
            .launchIn(appScope)
    }

    private fun schedulePeriodicWorker() {
        Timber.v("Setting up periodic worker for booster notification check")
        workManager.enqueueUniquePeriodicWork(UNIQUE_WORKER_NAME, ExistingPeriodicWorkPolicy.KEEP, buildWorkRequest())
    }

    private fun buildWorkRequest() = PeriodicWorkRequestBuilder<BoosterCheckWorker>(24, TimeUnit.HOURS)
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            BackgroundConstants.KIND_DELAY,
            TimeUnit.MINUTES
        )
        .build()
}

private const val UNIQUE_WORKER_NAME = "BoosterCheckWorker"
