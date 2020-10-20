package de.rki.coronawarnapp.ui.settings.crash

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SettingsCrashReportDetailsFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(SettingsCrashReportViewModel::class)
    abstract fun settingsCrashReportDetailsFragment(factory: SettingsCrashReportViewModel.Factory): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun settingsCrashReportDetailsFragment(): SettingsCrashReportDetailsFragment
}
