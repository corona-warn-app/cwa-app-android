package de.rki.coronawarnapp.dccreissuance

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceConsentFragment
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceConsentFragmentModule

@Module
abstract class DccReissuanceUiModule {

    @ContributesAndroidInjector(modules = [DccReissuanceConsentFragmentModule::class])
    abstract fun dccReissuanceConsentFragment(): DccReissuanceConsentFragment
}
