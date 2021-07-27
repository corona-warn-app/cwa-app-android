package de.rki.coronawarnapp.covidcertificate.common.statecheck

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.type.pcr.execution.PCRResultRetrievalWorker
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationNotificationService
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.joda.time.Duration
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccStateCheckScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val foregroundState: ForegroundState,
    private val workManager: WorkManager,
    private val dccExpirationNotificationService: DccExpirationNotificationService,
    private val dscRepository: DscRepository,
    private val timeStamper: TimeStamper,
) {

    fun setup() {
        Timber.d("setup()")

        foregroundState.isInForeground
            .onStart {
                Timber.tag(TAG).v("Monitoring foregroundstate (expiration checks) ")
                // Due to KEEP policy, we just call to make sure it's scheduled
                schedulePeriodicWorker()
            }
            .distinctUntilChanged()
            .filter { it } // Only when going into foreground
            .onEach {
                dccExpirationNotificationService.showNotificationIfExpired()
            }
            .launchIn(appScope)

        foregroundState.isInForeground
            .onStart { Timber.tag(TAG).v("Monitoring foregroundstate (dsc downloads) ") }
            .distinctUntilChanged()
            .filter { it } // Only when going into foreground
            .onEach {
                val currentDscData = dscRepository.dscData.first()
                if (Duration(currentDscData.updatedAt, timeStamper.nowUTC) < Duration.standardHours(12)) {
                    Timber.tag(TAG).d("Last DSC data refresh was recent: %s", currentDscData.updatedAt)
                    return@onEach
                }

                try {
                    Timber.tag(TAG).i("Refreshing DSC data.")
                    dscRepository.refresh()
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Refreshing DSC data failed.")
                }
            }
            .launchIn(appScope)
    }

    private fun schedulePeriodicWorker() {
        Timber.tag(TAG).v("Setting up periodic worker for dcc state check.")
        workManager.enqueueUniquePeriodicWork(UNIQUE_WORKERNAME, ExistingPeriodicWorkPolicy.KEEP, buildWorkRequest())
    }

    private fun buildWorkRequest() = PeriodicWorkRequestBuilder<PCRResultRetrievalWorker>(24, TimeUnit.HOURS)
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            BackgroundConstants.KIND_DELAY,
            TimeUnit.MINUTES
        )
        .build()

    companion object {
        private const val TAG = "DccStateCheckScheduler"
        private const val UNIQUE_WORKERNAME = "DccStateCheckWorker"
    }
}
