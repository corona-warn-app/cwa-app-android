package de.rki.coronawarnapp.ui.launcher

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class LauncherActivityModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(LauncherActivityViewModel::class)
    abstract fun launcherActivity(
        factory: LauncherActivityViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
