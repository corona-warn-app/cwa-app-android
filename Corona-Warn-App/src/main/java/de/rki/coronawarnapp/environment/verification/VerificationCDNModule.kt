package de.rki.coronawarnapp.environment.verification

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import javax.inject.Singleton

@Module
class VerificationCDNModule : BaseEnvironmentModule() {

    @Singleton
    @VerificationCDNServerUrl
    @Provides
    fun provideVerificationUrl(environment: EnvironmentSetup): String {
        val url = environment.cdnUrlVerification
        requireValidUrl(url)
        return url
    }
}
