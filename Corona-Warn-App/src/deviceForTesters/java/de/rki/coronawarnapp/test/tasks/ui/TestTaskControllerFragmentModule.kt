package de.rki.coronawarnapp.test.tasks.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TestTaskControllerFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TestTaskControllerFragmentViewModel::class)
    abstract fun testTaskControllerFragment(
        factory: TestTaskControllerFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
