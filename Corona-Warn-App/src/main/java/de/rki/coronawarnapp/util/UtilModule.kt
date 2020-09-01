package de.rki.coronawarnapp.util

import dagger.Binds
import dagger.Module
import de.rki.coronawarnapp.util.device.DefaultPowerManagement
import de.rki.coronawarnapp.util.device.PowerManagement
import javax.inject.Singleton

@Module
abstract class UtilModule {

    @Binds
    @Singleton
    abstract fun bindPowerManagement(
        powerManagement: DefaultPowerManagement
    ): PowerManagement

    @Binds
    @Singleton
    abstract fun bindBackgroundPrioritization(
        backgroundPrioritization: DefaultBackgroundPrioritization
    ): BackgroundPrioritization
}
