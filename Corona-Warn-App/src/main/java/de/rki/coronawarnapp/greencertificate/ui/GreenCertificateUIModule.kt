package de.rki.coronawarnapp.greencertificate.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesFragment
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesFragmentModule
import de.rki.coronawarnapp.greencertificate.ui.certificates.details.CertificateDetailsFragment
import de.rki.coronawarnapp.greencertificate.ui.certificates.details.CertificateDetailsModule
import de.rki.coronawarnapp.vaccination.ui.consent.VaccinationConsentFragment
import de.rki.coronawarnapp.vaccination.ui.consent.VaccinationConsentFragmentModule

@Module
abstract class GreenCertificateUIModule {

    @ContributesAndroidInjector(modules = [CertificatesFragmentModule::class])
    abstract fun certificatesFragment(): CertificatesFragment

    @ContributesAndroidInjector(modules = [CertificateDetailsModule::class])
    abstract fun certificateDetailsFragment(): CertificateDetailsFragment
}
