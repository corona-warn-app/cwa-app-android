package de.rki.coronawarnapp.environment.verification

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class VerificationCDNModule : BaseEnvironmentModule() {

    @Singleton
    @VerificationCDNServerUrl
    @Provides
    fun provideVerificationUrl(environment: EnvironmentSetup): String {
        val url = environment.verificationCdnUrl
        return requireValidUrl(url)
    }
}
