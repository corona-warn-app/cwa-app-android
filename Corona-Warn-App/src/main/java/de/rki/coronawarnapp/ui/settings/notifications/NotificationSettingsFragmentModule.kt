package de.rki.coronawarnapp.ui.settings.notifications

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class NotificationSettingsFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(NotificationSettingsFragmentViewModel::class)
    abstract fun homeFragment(
        factory: NotificationSettingsFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun riskDetails(): NotificationSettingsFragment
}
