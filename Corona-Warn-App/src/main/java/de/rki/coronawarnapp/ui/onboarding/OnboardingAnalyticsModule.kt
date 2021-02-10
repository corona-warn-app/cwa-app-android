package de.rki.coronawarnapp.ui.onboarding

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class OnboardingAnalyticsModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(OnboardingAnalyticsViewModel::class)
    abstract fun onboardingScreenVM(
        factory: OnboardingAnalyticsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun onboardingScreen(): OnboardingFragment
}
