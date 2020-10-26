package de.rki.coronawarnapp.ui.tracing.details

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class RiskDetailsFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(RiskDetailsFragmentViewModel::class)
    abstract fun homeFragment(
        factory: RiskDetailsFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun riskDetails(): RiskDetailsFragment
}
