package de.rki.coronawarnapp.bugreporting.debuglog.ui

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DebugLogFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(DebugLogViewModel::class)
    abstract fun onboardingNotificationsVM(factory: DebugLogViewModel.Factory): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun debuglogFragment(): DebugLogFragment
}
