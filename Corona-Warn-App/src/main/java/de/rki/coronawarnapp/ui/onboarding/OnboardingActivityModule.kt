package de.rki.coronawarnapp.ui.onboarding

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class OnboardingActivityModule {

    // activity specific injection module for future dependencies

    // example:
    // @ContributesAndroidInjector
    // abstract fun onboardingFragment(): OnboardingFragment

    @ContributesAndroidInjector(modules = [OnboardingTracingModule::class])
    abstract fun onboardingScreen(): OnboardingTracingFragment
}
