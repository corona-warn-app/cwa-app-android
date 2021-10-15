package de.rki.coronawarnapp.reyclebin.cleanup

import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecycleBinCleanUpScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val foregroundState: ForegroundState,
    private val recycleBinCleanUpService: RecycleBinCleanUpService
) {

    fun setup() {
        Timber.d("setup()")
    }
}
