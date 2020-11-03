package de.rki.coronawarnapp.util

import android.content.Context
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundModeStatus @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope
) {

    val isBackgroundRestricted: Flow<Boolean> = callbackFlow<Boolean> {
        var isRunning = true
        while (isRunning && isActive) {
            try {
                sendBlocking(pollIsBackgroundRestricted())
            } catch (e: Exception) {
                Timber.w(e, "isBackgroundRestricted failed.")
                cancel("isBackgroundRestricted failed", e)
            }
            delay(POLLING_DELAY_MS)
        }
        awaitClose { isRunning = false }
    }
        .distinctUntilChanged()
        .shareLatest(
            tag = "isBackgroundRestricted",
            scope = appScope
        )

    val isAutoModeEnabled: Flow<Boolean> = callbackFlow<Boolean> {
        var isRunning = true
        while (isRunning && isActive) {
            try {
                sendBlocking(pollIsAutoMode())
            } catch (e: Exception) {
                Timber.w(e, "autoModeEnabled failed.")
                cancel("autoModeEnabled failed", e)
            }
            delay(POLLING_DELAY_MS)
        }
        awaitClose { isRunning = false }
    }
        .distinctUntilChanged()
        .shareLatest(
            tag = "autoModeEnabled",
            scope = appScope
        )

    private fun pollIsBackgroundRestricted(): Boolean {
        return ConnectivityHelper.isBackgroundRestricted(context)
    }

    private fun pollIsAutoMode(): Boolean {
        return ConnectivityHelper.autoModeEnabled(context)
    }

    companion object {
        private const val POLLING_DELAY_MS = 1000L
    }
}
