package de.rki.coronawarnapp.covidcertificate.vaccination.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.consent.VaccinationConsentFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.consent.VaccinationConsentFragmentModule
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragmentModule
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.VaccinationListFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.VaccinationListFragmentModule

@Module
abstract class VaccinationCertificateUIModule {

    @ContributesAndroidInjector(modules = [VaccinationListFragmentModule::class])
    abstract fun vaccinationListFragment(): VaccinationListFragment

    @ContributesAndroidInjector(modules = [VaccinationDetailsFragmentModule::class])
    abstract fun vaccinationDetailsFragment(): VaccinationDetailsFragment

    @ContributesAndroidInjector(modules = [VaccinationConsentFragmentModule::class])
    abstract fun vaccinationConsentFragment(): VaccinationConsentFragment
}
