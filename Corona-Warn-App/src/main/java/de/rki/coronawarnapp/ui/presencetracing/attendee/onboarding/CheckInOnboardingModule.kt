package de.rki.coronawarnapp.ui.presencetracing.attendee.onboarding

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CheckInOnboardingModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(CheckInOnboardingViewModel::class)
    abstract fun checkInOnboardingFragment(
        factory: CheckInOnboardingViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
