package de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DccReissuanceAccCertsFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(DccReissuanceAccCertsViewModel::class)
    abstract fun dccReissuanceConsentFragment(
        factory: DccReissuanceAccCertsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
