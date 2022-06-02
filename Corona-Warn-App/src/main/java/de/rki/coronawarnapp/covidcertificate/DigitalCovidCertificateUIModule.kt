package de.rki.coronawarnapp.covidcertificate

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.boosterinfodetails.BoosterInfoDetailsFragment
import de.rki.coronawarnapp.covidcertificate.boosterinfodetails.BoosterInfoDetailsFragmentModule
import de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.DccExportAllOverviewFragment
import de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.DccExportAllOverviewModule
import de.rki.coronawarnapp.covidcertificate.pdf.ui.poster.CertificatePosterFragment
import de.rki.coronawarnapp.covidcertificate.pdf.ui.poster.CertificatePosterModule
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.AdmissionScenarioFragmentModule
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.AdmissionScenariosFragment
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsFragment
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsFragmentModule
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewFragment
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewFragmentModule
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsModule
import de.rki.coronawarnapp.covidcertificate.signature.ui.DigitalSignatureCertificateUIModule
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsModule
import de.rki.coronawarnapp.covidcertificate.ui.onboarding.CovidCertificateOnboardingFragment
import de.rki.coronawarnapp.covidcertificate.ui.onboarding.CovidCertificateOnboardingFragmentModule
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragmentModule
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.DccValidationResultModule
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart.ValidationStartFragment
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart.ValidationStartModule

@Module(
    includes = [
        DccValidationResultModule::class,
        DigitalSignatureCertificateUIModule::class,
    ]
)
abstract class DigitalCovidCertificateUIModule {

    @ContributesAndroidInjector(modules = [PersonOverviewFragmentModule::class])
    abstract fun personOverviewFragment(): PersonOverviewFragment

    @ContributesAndroidInjector(modules = [PersonDetailsFragmentModule::class])
    abstract fun personDetailsFragment(): PersonDetailsFragment

    @ContributesAndroidInjector(modules = [VaccinationDetailsFragmentModule::class])
    abstract fun vaccinationDetailsFragment(): VaccinationDetailsFragment

    @ContributesAndroidInjector(modules = [TestCertificateDetailsModule::class])
    abstract fun certificateDetailsFragment(): TestCertificateDetailsFragment

    @ContributesAndroidInjector(modules = [CovidCertificateOnboardingFragmentModule::class])
    abstract fun vaccinationConsentFragment(): CovidCertificateOnboardingFragment

    @ContributesAndroidInjector(modules = [RecoveryCertificateDetailsModule::class])
    abstract fun recoveryCertificateDetailsFragment(): RecoveryCertificateDetailsFragment

    @ContributesAndroidInjector(modules = [ValidationStartModule::class])
    abstract fun validationStartFragment(): ValidationStartFragment

    @ContributesAndroidInjector(modules = [CertificatePosterModule::class])
    abstract fun certificatePosterFragment(): CertificatePosterFragment

    @ContributesAndroidInjector(modules = [BoosterInfoDetailsFragmentModule::class])
    abstract fun boosterInfodetailsFragment(): BoosterInfoDetailsFragment

    @ContributesAndroidInjector(modules = [AdmissionScenarioFragmentModule::class])
    abstract fun admissionScenariosFragment(): AdmissionScenariosFragment

    @ContributesAndroidInjector(modules = [DccExportAllOverviewModule::class])
    abstract fun dccExportAllOverviewFragment(): DccExportAllOverviewFragment
}
