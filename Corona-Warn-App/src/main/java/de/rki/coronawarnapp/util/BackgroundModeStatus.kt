package de.rki.coronawarnapp.util

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundModeStatus @Inject constructor(
    @AppContext private val context: Context
) {

    val isBackgroundRestricted: Flow<Boolean> = callbackFlow<Boolean> {
        var isRunning = true
        while (isRunning) {
            try {
                sendBlocking(pollIsBackgroundRestricted())
            } catch (e: Exception) {
                Timber.w(e, "isBackgroundRestricted failed.")
                cancel("isBackgroundRestricted failed", e)
            }
            delay(1000L)
        }
        awaitClose { isRunning = false }
    }
        .distinctUntilChanged()
        .onStart { Timber.v("isBackgroundRestricted FLOW start") }
        .onEach { Timber.v("isBackgroundRestricted FLOW emission: %b", it) }
        .onCompletion { Timber.v("isBackgroundRestricted FLOW completed.") }

    val isAutoModeEnabled: Flow<Boolean> = callbackFlow<Boolean> {
        var isRunning = true
        while (isRunning) {
            try {
                sendBlocking(pollIsAutoMode())
            } catch (e: Exception) {
                Timber.w(e, "autoModeEnabled failed.")
                cancel("autoModeEnabled failed", e)
            }
            delay(1000L)
        }
        awaitClose { isRunning = false }
    }
        .distinctUntilChanged()
        .onStart { Timber.v("autoModeEnabled FLOW start") }
        .onEach { Timber.v("autoModeEnabled FLOW emission: %b", it) }
        .onCompletion { Timber.v("autoModeEnabled FLOW completed.") }

    private fun pollIsBackgroundRestricted(): Boolean {
        return ConnectivityHelper.isBackgroundRestricted(context)
    }

    private fun pollIsAutoMode(): Boolean {
        return ConnectivityHelper.autoModeEnabled(context)
    }
}
