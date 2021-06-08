package de.rki.coronawarnapp.covidcertificate

import dagger.Module
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateServerModule
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationModule
import de.rki.coronawarnapp.covidcertificate.valueset.CertificateValueSetModule

@Module(
    includes = [
        CertificateValueSetModule::class,
        TestCertificateServerModule::class,
        VaccinationModule::class,
    ]
)
abstract class DigitalCovidCertificateModule
