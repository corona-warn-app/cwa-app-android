package de.rki.coronawarnapp.ui.interoperability

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class InteroperabilityConfigurationFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(InteroperabilityConfigurationFragmentViewModel::class)
    abstract fun testRiskLevelFragment(factory: InteroperabilityConfigurationFragmentViewModel.Factory): CWAViewModelFactory<out CWAViewModel>
}
