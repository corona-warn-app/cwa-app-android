package de.rki.coronawarnapp.test.deltaonboarding.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DeltaOnboardingFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(DeltaOnboardingFragmentViewModel::class)
    abstract fun testTaskControllerFragment(
        factory: DeltaOnboardingFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
