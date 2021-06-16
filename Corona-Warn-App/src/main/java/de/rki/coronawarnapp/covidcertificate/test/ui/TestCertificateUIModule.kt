package de.rki.coronawarnapp.covidcertificate.test.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsFragment
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsFragmentModule
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewFragment
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewFragmentModule
import de.rki.coronawarnapp.covidcertificate.test.ui.details.CovidCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.details.CovidCertificateDetailsModule

@Module
abstract class TestCertificateUIModule {

    @ContributesAndroidInjector(modules = [CertificatesFragmentModule::class])
    abstract fun certificatesFragment(): CertificatesFragment

    @ContributesAndroidInjector(modules = [CovidCertificateDetailsModule::class])
    abstract fun certificateDetailsFragment(): CovidCertificateDetailsFragment

    @ContributesAndroidInjector(modules = [PersonOverviewFragmentModule::class])
    abstract fun personOverviewFragment(): PersonOverviewFragment

    @ContributesAndroidInjector(modules = [PersonDetailsFragmentModule::class])
    abstract fun personDetailsFragment(): PersonDetailsFragment
}
