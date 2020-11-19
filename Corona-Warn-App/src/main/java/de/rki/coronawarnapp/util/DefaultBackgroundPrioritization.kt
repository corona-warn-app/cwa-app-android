package de.rki.coronawarnapp.util

import dagger.Reusable
import de.rki.coronawarnapp.util.device.PowerManagement
import javax.inject.Inject

@Reusable
class DefaultBackgroundPrioritization @Inject constructor(
    private val powerManagement: PowerManagement
) : BackgroundPrioritization {

    override val isBackgroundActivityPrioritized
        get() = powerManagement.isIgnoringBatteryOptimizations
}
