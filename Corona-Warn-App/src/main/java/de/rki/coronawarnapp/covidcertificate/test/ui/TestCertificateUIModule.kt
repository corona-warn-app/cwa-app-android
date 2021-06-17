package de.rki.coronawarnapp.covidcertificate.test.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewFragment
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewFragmentModule
import de.rki.coronawarnapp.covidcertificate.test.ui.details.CovidCertificateDetailsModule
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment

@Module
abstract class TestCertificateUIModule {

    @ContributesAndroidInjector(modules = [CertificatesFragmentModule::class])
    abstract fun certificatesFragment(): CertificatesFragment

    @ContributesAndroidInjector(modules = [CovidCertificateDetailsModule::class])
    abstract fun certificateDetailsFragment(): TestCertificateDetailsFragment

    @ContributesAndroidInjector(modules = [PersonOverviewFragmentModule::class])
    abstract fun personOverviewFragment(): PersonOverviewFragment
}
