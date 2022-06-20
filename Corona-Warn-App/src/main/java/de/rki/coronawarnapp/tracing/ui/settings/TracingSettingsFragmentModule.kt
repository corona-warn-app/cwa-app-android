package de.rki.coronawarnapp.tracing.ui.settings

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TracingSettingsFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TracingSettingsFragmentViewModel::class)
    abstract fun homeFragment(
        factory: TracingSettingsFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun riskDetails(): TracingSettingsFragment
}
