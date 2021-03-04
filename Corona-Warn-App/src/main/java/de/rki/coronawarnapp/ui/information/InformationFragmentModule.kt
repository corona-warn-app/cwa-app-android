package de.rki.coronawarnapp.ui.information

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.bugreporting.debuglog.ui.DebugLogFragmentModule
import de.rki.coronawarnapp.bugreporting.debuglog.ui.legal.DebugLogLegalModule
import de.rki.coronawarnapp.bugreporting.debuglog.ui.upload.DebugLogUploadFragmentModule
import de.rki.coronawarnapp.bugreporting.uploadhistory.ui.LogUploadHistoryModule
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module(
    includes = [
        DebugLogFragmentModule::class,
        LogUploadHistoryModule::class,
        DebugLogLegalModule::class
        DebugLogUploadFragmentModule::class
    ]
)
abstract class InformationFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(InformationFragmentViewModel::class)
    abstract fun informationFragmentViewModel(
        factory: InformationFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun informationFragment(): InformationFragment
}
