package de.rki.coronawarnapp.covidcertificate.test.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.test.ui.details.CovidCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.details.CovidCertificateDetailsModule

@Module
abstract class CovidCertificateUIModule {

    @ContributesAndroidInjector(modules = [CertificatesFragmentModule::class])
    abstract fun certificatesFragment(): CertificatesFragment

    @ContributesAndroidInjector(modules = [CovidCertificateDetailsModule::class])
    abstract fun certificateDetailsFragment(): CovidCertificateDetailsFragment
}
