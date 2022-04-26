package de.rki.coronawarnapp.covidcertificate.revocation.update

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccRevocationUpdateScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val revocationListUpdater: DccRevocationListUpdater,
    private val foregroundState: ForegroundState,
    private val workManager: WorkManager
) {

    fun setup() {
        Timber.tag(TAG).d("setup()")
        foregroundState.isInForeground
            .onStart { scheduleDailyWorker() }
            .distinctUntilChanged()
            .filter { it }
            .onEach { triggerUpdate(forceUpdate = false) }
            .catch { Timber.tag(TAG).e(it, "Failed to schedule work") }
            .launchIn(scope = appScope)
    }

    fun forceUpdate() = appScope.launch {
        Timber.tag(TAG).d("forceUpdate()")
        triggerUpdate(forceUpdate = true)
    }

    private suspend fun triggerUpdate(forceUpdate: Boolean) = revocationListUpdater.updateRevocationList(forceUpdate)

    private fun scheduleDailyWorker() {
        Timber.tag(TAG).d("scheduleDailyWorker()")
        workManager.enqueueUniquePeriodicWork(WORKER_NAME, ExistingPeriodicWorkPolicy.KEEP, buildWorkRequest())
    }

    private fun buildWorkRequest(): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<DccRevocationListUpdateWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).setInitialDelay(
            BackgroundConstants.KIND_DELAY,
            TimeUnit.MINUTES
        ).build()
    }
}

private const val WORKER_NAME = "RevocationListUpdateWorker"
private val TAG = tag<DccRevocationUpdateScheduler>()
