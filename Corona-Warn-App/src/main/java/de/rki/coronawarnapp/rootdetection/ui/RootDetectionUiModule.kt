package de.rki.coronawarnapp.rootdetection.ui

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class RootDetectionUiModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(RootDetectionDialogViewModel::class)
    abstract fun rootDetectionDialogViewModel(
        factory: RootDetectionDialogViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun rootDetectionDialogFragment(): RootDetectionDialogFragment
}
