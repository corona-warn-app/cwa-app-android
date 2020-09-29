package de.rki.coronawarnapp.environment.submission

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import javax.inject.Singleton

@Module
class SubmissionCDNModule : BaseEnvironmentModule() {

    @Singleton
    @SubmissionCDNServerUrl
    @Provides
    fun provideSubmissionUrl(environment: EnvironmentSetup): String {
        val url = environment.submissionCdnUrl
        return requireValidUrl(url)
    }
}
