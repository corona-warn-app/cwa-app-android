package de.rki.coronawarnapp.util.device

import android.annotation.TargetApi
import android.app.ActivityManager
import android.os.Build
import de.rki.coronawarnapp.util.ApiLevel
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundModeStatus @Inject constructor(
    foregroundState: ForegroundState,
    private val powerManagement: PowerManagement,
    private val activityManager: ActivityManager,
    private val apiLevel: ApiLevel,
    @AppScope private val appScope: CoroutineScope
) {

    val isBackgroundRestricted: Flow<Boolean> = flow {
        while (true) {
            try {
                emit(pollIsBackgroundRestricted())
                delay(POLLING_DELAY_MS)
            } catch (e: CancellationException) {
                Timber.d("isBackgroundRestricted was cancelled")
                break
            }
        }
    }
        .distinctUntilChanged()
        .onCompletion {
            if (it != null) Timber.w(it, "isBackgroundRestricted failed.")
        }
        .shareLatest(
            tag = "isBackgroundRestricted",
            scope = appScope
        )

    /**
     * For API level 28+ check if background is restricted
     * Else always return false
     */
    @TargetApi(Build.VERSION_CODES.P)
    private fun pollIsBackgroundRestricted(): Boolean = if (apiLevel.hasAPILevel(Build.VERSION_CODES.P)) {
        activityManager.isBackgroundRestricted
    } else false

    val isAutoModeEnabled: Flow<Boolean> = flow {
        while (true) {
            try {
                emit(pollIsAutoMode())
                delay(POLLING_DELAY_MS)
            } catch (e: CancellationException) {
                Timber.d("isAutoModeEnabled was cancelled")
                break
            }
        }
    }
        .distinctUntilChanged()
        .onCompletion {
            if (it != null) Timber.w(it, "autoModeEnabled failed.")
        }
        .shareLatest(
            tag = "autoModeEnabled",
            scope = appScope
        )

    /**
     * Background jobs are enabled only if the background activity prioritization is enabled and
     * the background activity is not restricted
     */
    private fun pollIsAutoMode(): Boolean {
        return !pollIsBackgroundRestricted() || powerManagement.isIgnoringBatteryOptimizations
    }

    val isIgnoringBatteryOptimizations: Flow<Boolean> = foregroundState.isInForeground
        .map { pollisIgnoringBatteryOptimizations() }
        .onStart { emit(pollisIgnoringBatteryOptimizations()) }
        .distinctUntilChanged()
        .onCompletion {
            if (it != null) Timber.w(it, "isIgnoringBatteryOptimizations failed.")
        }
        .shareLatest(
            tag = "isIgnoringBatteryOptimizations",
            scope = appScope
        )

    private fun pollisIgnoringBatteryOptimizations(): Boolean {
        return powerManagement.isIgnoringBatteryOptimizations
    }

    companion object {
        private const val POLLING_DELAY_MS = 1000L
    }
}
