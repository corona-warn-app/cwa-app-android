package de.rki.coronawarnapp.covidcertificate

import dagger.Module
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateModule
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateServerModule
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificateModule
import de.rki.coronawarnapp.covidcertificate.valueset.CertificateValueSetModule

@Module(
    includes = [
        CertificateValueSetModule::class,
        VaccinationCertificateModule::class,
        TestCertificateServerModule::class,
        RecoveryCertificateModule::class,
    ]
)
abstract class DigitalCovidCertificateModule
