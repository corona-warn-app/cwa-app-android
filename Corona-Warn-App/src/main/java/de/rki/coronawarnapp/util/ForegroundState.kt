package de.rki.coronawarnapp.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import de.rki.coronawarnapp.CoronaWarnApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForegroundState @Inject constructor() {

    val isInForeground: Flow<Boolean> by lazy {
        MutableStateFlow(false).apply {
            val foregroundStateUpdater = object : LifecycleObserver {
                @Suppress("unused")
                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                fun onAppForegrounded() {
                    CoronaWarnApplication.isAppInForeground = true
                    Timber.v("App is in the foreground")
                }

                @Suppress("unused")
                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                fun onAppBackgrounded() {
                    CoronaWarnApplication.isAppInForeground = false
                    Timber.v("App is in the background")
                }
            }

            val processLifecycle = ProcessLifecycleOwner.get().lifecycle
            processLifecycle.addObserver(foregroundStateUpdater)
        }
            .onStart { Timber.v("isInForeground FLOW start") }
            .onEach { Timber.v("isInForeground FLOW emission: %b", it) }
            .onCompletion { Timber.v("isInForeground FLOW completed.") }
    }
}
