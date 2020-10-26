package de.rki.coronawarnapp.ui.settings.start

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SettingsFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SettingsFragmentViewModel::class)
    abstract fun homeFragment(
        factory: SettingsFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun settingsStartFragment(): SettingsFragment
}
