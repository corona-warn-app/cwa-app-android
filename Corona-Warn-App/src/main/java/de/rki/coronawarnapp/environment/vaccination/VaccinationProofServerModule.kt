package de.rki.coronawarnapp.environment.vaccination

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
object VaccinationProofServerModule : BaseEnvironmentModule() {

    @Singleton
    @Provides
    fun provideVaccinationProofServerUrl(environmentSetup: EnvironmentSetup): String = environmentSetup
        .vaccinationProofServerUrl
        .let { requireValidUrl(it) }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class VaccinationProofServerUrl
