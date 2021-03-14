package de.rki.coronawarnapp.tracing.ui.settings

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SettingsTracingFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SettingsTracingFragmentViewModel::class)
    abstract fun homeFragment(
        factory: SettingsTracingFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun riskDetails(): SettingsTracingFragment
}
