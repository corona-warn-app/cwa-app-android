package de.rki.coronawarnapp.covidcertificate.test.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.CertificatesFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.CertificatesFragmentModule
import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.details.CovidCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.details.CovidCertificateDetailsModule

@Module
abstract class CovidCertificateUIModule {

    @ContributesAndroidInjector(modules = [CertificatesFragmentModule::class])
    abstract fun certificatesFragment(): CertificatesFragment

    @ContributesAndroidInjector(modules = [CovidCertificateDetailsModule::class])
    abstract fun certificateDetailsFragment(): CovidCertificateDetailsFragment
}
