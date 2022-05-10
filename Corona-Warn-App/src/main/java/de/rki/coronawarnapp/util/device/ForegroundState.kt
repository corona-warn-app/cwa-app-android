package de.rki.coronawarnapp.util.device

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.rki.coronawarnapp.util.di.ProcessLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForegroundState @Inject constructor(
    @ProcessLifecycle val processLifecycleOwner: LifecycleOwner
) {

    val isInForeground: Flow<Boolean> by lazy {
        MutableStateFlow(false).apply {
            val foregroundStateUpdater = object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    super.onStart(owner)
                    Timber.v("App is in the foreground")
                    tryEmit(true)
                }

                override fun onStop(owner: LifecycleOwner) {
                    super.onStop(owner)
                    Timber.v("App is in the background")
                    tryEmit(false)
                }
            }

            val processLifecycle = processLifecycleOwner.lifecycle
            processLifecycle.addObserver(foregroundStateUpdater)
        }.onStart {
            Timber.v("isInForeground FLOW start")
        }.onEach {
            Timber.v("isInForeground FLOW emission: %b", it)
        }.onCompletion {
            Timber.v("isInForeground FLOW completed.")
        }
    }
}
