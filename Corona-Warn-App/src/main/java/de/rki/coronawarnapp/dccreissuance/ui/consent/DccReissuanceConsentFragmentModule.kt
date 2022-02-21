package de.rki.coronawarnapp.dccreissuance.ui.consent

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DccReissuanceConsentFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(DccReissuanceConsentViewModel::class)
    abstract fun dccReissuanceConsentFragment(
        factory: DccReissuanceConsentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
