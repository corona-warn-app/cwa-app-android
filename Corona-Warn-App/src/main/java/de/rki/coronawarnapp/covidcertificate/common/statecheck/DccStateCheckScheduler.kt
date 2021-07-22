package de.rki.coronawarnapp.covidcertificate.common.statecheck

import de.rki.coronawarnapp.util.device.ForegroundState
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccStateCheckScheduler @Inject constructor(
    private val foregroundState: ForegroundState,
) {

    fun setup() {
        Timber.d("setup()")
        // Download new dsc data when necessary
        // Schedule DccStateCheckWorker when necessary
    }
}
