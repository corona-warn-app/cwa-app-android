package de.rki.coronawarnapp.dccreissuance.ui.success

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DccReissuanceSuccessFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(DccReissuanceSuccessViewModel::class)
    abstract fun dccReissuanceSuccessFragment(
        factory: DccReissuanceSuccessViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
