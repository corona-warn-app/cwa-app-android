package de.rki.coronawarnapp.test.appconfig.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class AppConfigTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(AppConfigTestFragmentViewModel::class)
    abstract fun testTaskControllerFragment(
        factory: AppConfigTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
