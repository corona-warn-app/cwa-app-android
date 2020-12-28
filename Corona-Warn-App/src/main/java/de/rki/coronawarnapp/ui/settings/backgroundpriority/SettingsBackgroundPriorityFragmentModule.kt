package de.rki.coronawarnapp.ui.settings.backgroundpriority

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SettingsBackgroundPriorityFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SettingsBackgroundPriorityFragmentViewModel::class)
    abstract fun homeFragment(
        factory: SettingsBackgroundPriorityFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun backgroundPriority(): SettingsBackgroundPriorityFragment
}
