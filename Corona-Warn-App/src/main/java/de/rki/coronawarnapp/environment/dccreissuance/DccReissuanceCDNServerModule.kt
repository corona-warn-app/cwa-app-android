package de.rki.coronawarnapp.environment.dccreissuance

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import javax.inject.Singleton

@Module
object DccReissuanceCDNServerModule : BaseEnvironmentModule() {

    @Singleton
    @DccReissuanceServerURL
    @Provides
    fun provideDccReissuanceServerURL(environmentSetup: EnvironmentSetup): String = environmentSetup
        .dccReissuanceServerUrl
        .let { requireValidUrl(it) }
}
