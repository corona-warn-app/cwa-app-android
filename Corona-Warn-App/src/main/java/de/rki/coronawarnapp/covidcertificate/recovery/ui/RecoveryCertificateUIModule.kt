package de.rki.coronawarnapp.covidcertificate.recovery.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsModule

@Module
abstract class RecoveryCertificateUIModule {
    @ContributesAndroidInjector(modules = [RecoveryCertificateDetailsModule::class])
    abstract fun recoveryCertificateDetailsFragment(): RecoveryCertificateDetailsFragment
}
