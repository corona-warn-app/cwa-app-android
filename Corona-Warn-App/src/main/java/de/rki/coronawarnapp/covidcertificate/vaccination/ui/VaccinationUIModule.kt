package de.rki.coronawarnapp.covidcertificate.vaccination.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.consent.VaccinationConsentFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.consent.VaccinationConsentFragmentModule
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragmentModule
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.VaccinationListFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.VaccinationListFragmentModule
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.scan.VaccinationQrCodeScanFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.scan.VaccinationQrCodeScanModule

@Module
abstract class VaccinationUIModule {

    @ContributesAndroidInjector(modules = [VaccinationListFragmentModule::class])
    abstract fun vaccinationListFragment(): VaccinationListFragment

    @ContributesAndroidInjector(modules = [VaccinationDetailsFragmentModule::class])
    abstract fun vaccinationDetailsFragment(): VaccinationDetailsFragment

    @ContributesAndroidInjector(modules = [VaccinationQrCodeScanModule::class])
    abstract fun vaccinationQrCodeScanFragment(): VaccinationQrCodeScanFragment

    @ContributesAndroidInjector(modules = [VaccinationConsentFragmentModule::class])
    abstract fun vaccinationConsentFragment(): VaccinationConsentFragment
}
