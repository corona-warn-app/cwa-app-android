package de.rki.coronawarnapp.environment.vaccination

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup

@Module
class VaccinationCertificateUrlModule : BaseEnvironmentModule() {

    @Reusable
    @VaccinationCertificateProofServerUrl
    @Provides
    fun provideVaccinationProofUrl(environmentSetup: EnvironmentSetup): String =
        requireValidUrl(environmentSetup.vaccinationProofServerUrl)

    @Reusable
    @VaccinationCertificateValueSetCDNUrl
    @Provides
    fun provideVaccinationValueSetUrl(environmentSetup: EnvironmentSetup): String =
        requireValidUrl(environmentSetup.vaccinationCdnUrl)
}
