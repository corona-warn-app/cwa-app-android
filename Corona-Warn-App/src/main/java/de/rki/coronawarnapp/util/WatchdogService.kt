package de.rki.coronawarnapp.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.worker.BackgroundWorkHelper
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchdogService @Inject constructor(
    @AppContext private val context: Context
) {

    private val powerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }
    private val wifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun launch() {
        // Only do this if the background jobs are enabled
        if (!ConnectivityHelper.autoModeEnabled(context)) {
            Timber.d("Background jobs are not enabled, aborting.")
            return
        }

        Timber.v("Acquiring wakelocks for watchdog routine.")
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            // A wakelock as the OS does not handle this for us like in the background job execution
            val wakeLock = createWakeLock()
            // A wifi lock to wake up the wifi connection in case the device is dozing
            val wifiLock = createWifiLock()
            try {
                BackgroundWorkHelper.sendDebugNotification(
                    "Automatic mode is on", "Check if we have downloaded keys already today"
                )
                RetrieveDiagnosisKeysTransaction.startWithConstraints()
            } catch (e: Exception) {
                BackgroundWorkHelper.sendDebugNotification(
                    "RetrieveDiagnosisKeysTransaction failed",
                    (e.localizedMessage
                        ?: "Unknown exception occurred in onCreate") + "\n\n" + (e.cause
                        ?: "Cause is unknown").toString()
                )
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
        private const val TEN_MINUTE_TIMEOUT_IN_MS = 10 * 60 * 1000L
    }
}
