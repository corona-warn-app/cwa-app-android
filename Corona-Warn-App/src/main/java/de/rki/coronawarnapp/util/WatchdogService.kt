package de.rki.coronawarnapp.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysTask
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.di.ProcessLifecycle
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchdogService @Inject constructor(
    @AppContext private val context: Context,
    private val taskController: TaskController,
    private val backgroundModeStatus: BackgroundModeStatus,
    @ProcessLifecycle private val processLifecycleOwner: LifecycleOwner
) {

    private val powerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }
    private val wifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun launch() {
        val isAutoModeEnable = runBlocking { backgroundModeStatus.isAutoModeEnabled.first() }
        // Only do this if the background jobs are enabled
        if (!isAutoModeEnable) {
            Timber.tag(TAG).d("Background jobs are not enabled, aborting.")
            return
        }

        Timber.tag(TAG).v("Acquiring wakelocks for watchdog routine.")
        processLifecycleOwner.lifecycleScope.launch {
            // A wakelock as the OS does not handle this for us like in the background job execution
            val wakeLock = createWakeLock()
            // A wifi lock to wake up the wifi connection in case the device is dozing
            val wifiLock = createWifiLock()

            Timber.tag(TAG).d("Automatic mode is on, check if we have downloaded keys already today")

            val state = taskController.submitBlocking(
                DefaultTaskRequest(
                    DownloadDiagnosisKeysTask::class,
                    DownloadDiagnosisKeysTask.Arguments(),
                    originTag = "WatchdogService"
                )
            )
            if (state.isFailed) {
                Timber.tag(TAG).e(state.error, "RetrieveDiagnosisKeysTransaction failed")
                // retry the key retrieval in case of an error with a scheduled work
                BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork()
            }

            if (wifiLock.isHeld) wifiLock.release()
            if (wakeLock.isHeld) wakeLock.release()
        }

        // if the user is onboarded we will schedule period background jobs
        // in case the app was force stopped and woken up again by the Google WakeUpService
        if (LocalData.onboardingCompletedTimestamp() != null) BackgroundWorkScheduler.startWorkScheduler()
    }

    private fun createWakeLock(): PowerManager.WakeLock = powerManager
        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CWA-WAKE-${UUID.randomUUID()}")
        .apply { acquire(TEN_MINUTE_TIMEOUT_IN_MS) }

    private fun createWifiLock(): WifiManager.WifiLock = wifiManager
        .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "CWA-WIFI-${UUID.randomUUID()}")
        .apply { acquire() }

    companion object {
        private const val TAG = "WatchdogService"
        private const val TEN_MINUTE_TIMEOUT_IN_MS = 10 * 60 * 1000L
    }
}
