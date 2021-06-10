package de.rki.coronawarnapp.covidcertificate

import dagger.Module
import de.rki.coronawarnapp.covidcertificate.recovery.ui.RecoveryCertificateUIModule
import de.rki.coronawarnapp.covidcertificate.test.ui.TestCertificateUIModule
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.VaccinationCertificateUIModule

@Module(
    includes = [
        VaccinationCertificateUIModule::class,
        TestCertificateUIModule::class,
        RecoveryCertificateUIModule::class,
    ]
)
abstract class DigitalCovidCertificateUIModule
