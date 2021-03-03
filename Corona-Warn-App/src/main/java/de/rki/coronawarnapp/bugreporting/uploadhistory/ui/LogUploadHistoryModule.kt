package de.rki.coronawarnapp.bugreporting.uploadhistory.ui

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class LogUploadHistoryModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(LogUploadHistoryViewModel::class)
    abstract fun uploadHistoryViewModel(
        factory: LogUploadHistoryViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun uploadHistory(): LogUploadHistoryFragment
}
