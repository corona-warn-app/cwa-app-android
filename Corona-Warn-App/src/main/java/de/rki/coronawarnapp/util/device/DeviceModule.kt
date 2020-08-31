package de.rki.coronawarnapp.util.device

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class DeviceModule {

    @Binds
    @Singleton
    abstract fun bindSystemInfoProvider(
        systemInfoProvider: DefaultSystemInfoProvider
    ): SystemInfoProvider
}
