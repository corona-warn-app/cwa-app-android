package de.rki.coronawarnapp.environment.dccreissuance

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DccReissuanceCDNServerModule : BaseEnvironmentModule() {

    @Singleton
    @DccReissuanceServerURL
    @Provides
    fun provideDccReissuanceServerURL(environmentSetup: EnvironmentSetup): String = environmentSetup
        .dccReissuanceServerUrl
        .let { requireValidUrl(it) }
}
