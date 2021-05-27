package de.rki.coronawarnapp.greencertificate.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesFragment
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesFragmentModule

@Module
abstract class GreenCertificateUIModule {

    @ContributesAndroidInjector(modules = [CertificatesFragmentModule::class])
    abstract fun certificatesFragment(): CertificatesFragment
}
