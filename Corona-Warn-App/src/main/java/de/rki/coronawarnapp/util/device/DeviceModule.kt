package de.rki.coronawarnapp.util.device

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class DeviceModule {

    @Binds
    @Singleton
    abstract fun bindSystemInfoProvider(
        systemInfoProvider: DefaultSystemInfoProvider
    ): SystemInfoProvider
}
