package de.rki.coronawarnapp.bugreporting.debuglog.ui.share

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DebugLogShareFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(DebugLogShareViewModel::class)
    abstract fun debugLogShareFragmentVM(
        factory: DebugLogShareViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
