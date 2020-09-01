package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.util.device.PowerManagement
import javax.inject.Inject

class DefaultBackgroundPrioritization @Inject constructor(
    private val powerManagement: PowerManagement
) : BackgroundPrioritization {

    override val isBackgroundActivityPrioritized: Boolean
        get() = powerManagement.isIgnoringBatteryOptimizations
}
