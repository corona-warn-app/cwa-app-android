package de.rki.coronawarnapp.covidcertificate

import dagger.Module
import de.rki.coronawarnapp.covidcertificate.test.server.TestCertificateServerModule
import de.rki.coronawarnapp.covidcertificate.valueset.CertificateValueSetModule

@Module(
    includes = [
        CertificateValueSetModule::class,
        TestCertificateServerModule::class,
    ]
)
abstract class DigitalCovidCertificateModule
