package de.rki.coronawarnapp.dccreissuance

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceConsentFragment
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceConsentFragmentModule
import de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts.DccReissuanceAccCertsFragment
import de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts.DccReissuanceAccCertsFragmentModule

@Module
abstract class DccReissuanceUiModule {

    @ContributesAndroidInjector(modules = [DccReissuanceConsentFragmentModule::class])
    abstract fun dccReissuanceConsentFragment(): DccReissuanceConsentFragment

    @ContributesAndroidInjector(modules = [DccReissuanceAccCertsFragmentModule::class])
    abstract fun dccReissuanceAccCertsFragment(): DccReissuanceAccCertsFragment
}
