package de.rki.coronawarnapp.test.debugoptions.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DebugOptionsFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(DebugOptionsFragmentViewModel::class)
    abstract fun testTaskControllerFragment(
        factory: DebugOptionsFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
