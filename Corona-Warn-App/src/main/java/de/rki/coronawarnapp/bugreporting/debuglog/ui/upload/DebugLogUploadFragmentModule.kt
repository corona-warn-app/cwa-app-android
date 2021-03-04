package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DebugLogUploadFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(DebugLogUploadViewModel::class)
    abstract fun debugLogViewModel(factory: DebugLogUploadViewModel.Factory): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun debugLogFragment(): DebugLogUploadFragment
}
