package de.rki.coronawarnapp.reyclebin.cleanup

import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecycleBinCleanUpScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val foregroundState: ForegroundState,
    private val recycleBinCleanUpService: RecycleBinCleanUpService
) : Initializer {

    override fun initialize() {
        Timber.d("setup()")

        foregroundState.isInForeground
            .distinctUntilChanged()
            .filter { it } // Only when going into foreground
            .onEach { startCleanUpSafely() }
            .launchIn(appScope)
    }

    private suspend fun startCleanUpSafely(): Unit = try {
        Timber.v("startCleanUpSafely()")
        recycleBinCleanUpService.clearRecycledItems()
    } catch (e: Throwable) {
        Timber.e(e, "Clean up failed")
    }
}
