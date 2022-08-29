package de.rki.coronawarnapp.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.di.ProcessLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/*
* Triggers risk calculation when app comes into foreground,
* once at app start and at most every hour when while still alive
* */
@Singleton
class ForegroundRiskCalculationService @Inject constructor(
    @AppContext private val context: Context,
    @ProcessLifecycle private val processLifecycleOwner: LifecycleOwner,
    private val tracingRepository: TracingRepository,
    private val foregroundState: ForegroundState,
    @AppScope private val appScope: CoroutineScope,
    private val timeStamper: TimeStamper
) : Initializer {

    private val powerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }
    private val wifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val mutex = Mutex()

    private var latestRunTimeStamp: Instant = Instant.EPOCH

    override fun initialize() {
        foregroundState.isInForeground
            .distinctUntilChanged()
            .filter { it }
            .onEach { runRiskCalculations() }
            .launchIn(appScope)
    }

    private suspend fun runRiskCalculations() = mutex.withLock {
        if (Duration.between(latestRunTimeStamp, timeStamper.nowJavaUTC) < minTimeBetweenRuns) {
            Timber.tag(TAG).d("Min time between runs of $minTimeBetweenRuns not passed, aborting.")
            return@withLock
        }
        latestRunTimeStamp = timeStamper.nowJavaUTC

        // If we are being bound by Google Play Services (which is only a few seconds)
        // and don't have a worker or foreground service, the system may still kill us and the tasks
        // before they have finished executing.
        Timber.tag(TAG).v("Acquiring wakelocks for watchdog routine.")
        processLifecycleOwner.lifecycleScope.launch {
            // A wakelock as the OS does not handle this for us like in the background job execution
            val wakeLock = createWakeLock()
            // A wifi lock to wake up the wifi connection in case the device is dozing
            val wifiLock = createWifiLock()

            tracingRepository.runRiskCalculations()

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
        private const val TAG = "ForegroundRiskCalc"
        private const val TEN_MINUTE_TIMEOUT_IN_MS = 10 * 60 * 1000L
        private val minTimeBetweenRuns = Duration.ofMinutes(60)
    }
}
