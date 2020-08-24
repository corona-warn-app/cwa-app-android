package de.rki.coronawarnapp.ui.onboarding

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import javax.inject.Singleton

@Module
internal abstract class OnboardingActivityModule {

    // activity specific injection module for future dependencies

    // example:
    // @ContributesAndroidInjector
    // abstract fun onboardingFragment(): OnboardingFragment
}
