package de.rki.coronawarnapp.greencertificate.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesFragment
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesFragmentModule
import de.rki.coronawarnapp.greencertificate.ui.onboarding.CertificatesOnboardingFragment
import de.rki.coronawarnapp.greencertificate.ui.onboarding.CertificatesOnboardingFragmentModule

@Module
abstract class GreenCertificateUIModule {

    @ContributesAndroidInjector(modules = [CertificatesFragmentModule::class])
    abstract fun certificatesFragment(): CertificatesFragment

    @ContributesAndroidInjector(modules = [CertificatesOnboardingFragmentModule::class])
    abstract fun certificatesOnboardingFragment(): CertificatesOnboardingFragment
}
