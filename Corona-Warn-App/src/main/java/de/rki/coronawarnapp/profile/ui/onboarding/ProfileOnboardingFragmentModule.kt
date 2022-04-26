package de.rki.coronawarnapp.profile.ui.onboarding

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ProfileOnboardingFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ProfileOnboardingFragmentViewModel::class)
    abstract fun profileOnboardingFragment(
        factory: ProfileOnboardingFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
