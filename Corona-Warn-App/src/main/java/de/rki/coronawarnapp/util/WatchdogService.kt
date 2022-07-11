package de.rki.coronawarnapp.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingRiskWorkScheduler
import de.rki.coronawarnapp.risk.execution.ExposureWindowRiskWorkScheduler
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.di.ProcessLifecycle
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
    private val backgroundModeStatus: BackgroundModeStatus,
    @ProcessLifecycle private val processLifecycleOwner: LifecycleOwner,
    private val exposureWindowRiskWorkScheduler: ExposureWindowRiskWorkScheduler,
    private val presenceTracingRiskRepository: PresenceTracingRiskWorkScheduler,
) : Initializer {

    private val powerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }
    private val wifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    override fun initialize() {
        val isAutoModeEnable = runBlocking { backgroundModeStatus.isAutoModeEnabled.first() }
        // Only do this if the background jobs are enabled
        if (!isAutoModeEnable) {
            Timber.tag(TAG).d("Background jobs are not enabled, aborting.")
            return
        }

        // If we are being bound by Google Play Services (which is only a few seconds)
        // and don't have a worker or foreground service, the system may still kill us and the tasks
        // before they have finished executing.
        Timber.tag(TAG).v("Acquiring wakelocks for watchdog routine.")
        processLifecycleOwner.lifecycleScope.launch {
            // A wakelock as the OS does not handle this for us like in the background job execution
            val wakeLock = createWakeLock()
            // A wifi lock to wake up the wifi connection in case the device is dozing
            val wifiLock = createWifiLock()

            Timber.tag(TAG).d("Automatic mode is on, check if we have downloaded keys already today")

            Timber.tag(TAG).d("Running EW risk tasks now.")
            exposureWindowRiskWorkScheduler.runRiskTasksNow(TAG)

            Timber.tag(TAG).d("Rnuning PT risk tasks now.")
            presenceTracingRiskRepository.runRiskTaskNow(TAG)

            if (wifiLock.isHeld) wifiLock.release()
            if (wakeLock.isHeld) wakeLock.release()
        }
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
