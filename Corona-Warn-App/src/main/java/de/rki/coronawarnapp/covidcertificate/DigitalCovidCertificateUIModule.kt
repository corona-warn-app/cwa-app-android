package de.rki.coronawarnapp.covidcertificate

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.common.scan.DccQrCodeScanFragment
import de.rki.coronawarnapp.covidcertificate.common.scan.DccQrCodeScanModule
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsFragment
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsFragmentModule
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewFragment
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewFragmentModule
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsModule
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsModule
import de.rki.coronawarnapp.covidcertificate.ui.info.CovidCertificateInfoFragment
import de.rki.coronawarnapp.covidcertificate.ui.info.CovidCertificateInfoFragmentModule
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragmentModule

@Module
abstract class DigitalCovidCertificateUIModule {

    @ContributesAndroidInjector(modules = [DccQrCodeScanModule::class])
    abstract fun dccQrCodeScanFragment(): DccQrCodeScanFragment

    @ContributesAndroidInjector(modules = [PersonOverviewFragmentModule::class])
    abstract fun personOverviewFragment(): PersonOverviewFragment

    @ContributesAndroidInjector(modules = [PersonDetailsFragmentModule::class])
    abstract fun personDetailsFragment(): PersonDetailsFragment

    @ContributesAndroidInjector(modules = [VaccinationDetailsFragmentModule::class])
    abstract fun vaccinationDetailsFragment(): VaccinationDetailsFragment

    @ContributesAndroidInjector(modules = [TestCertificateDetailsModule::class])
    abstract fun certificateDetailsFragment(): TestCertificateDetailsFragment

    @ContributesAndroidInjector(modules = [CovidCertificateInfoFragmentModule::class])
    abstract fun vaccinationConsentFragment(): CovidCertificateInfoFragment

    @ContributesAndroidInjector(modules = [RecoveryCertificateDetailsModule::class])
    abstract fun recoveryCertificateDetailsFragment(): RecoveryCertificateDetailsFragment
}
