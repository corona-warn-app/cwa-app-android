package de.rki.coronawarnapp.bugreporting.debuglog.ui.legal

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DebugLogLegalModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(DebugLogLegalViewModel::class)
    abstract fun debugLogLegalViewModel(factory: DebugLogLegalViewModel.Factory): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun debugLogLegalFragment(): DebugLogLegalFragment
}
