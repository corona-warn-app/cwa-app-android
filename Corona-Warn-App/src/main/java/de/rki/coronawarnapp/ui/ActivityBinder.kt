package de.rki.coronawarnapp.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.main.MainActivityModule
import de.rki.coronawarnapp.ui.main.MainActivityTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivityModule

@Module
abstract class ActivityBinder {
    @ContributesAndroidInjector(modules = [MainActivityModule::class, MainActivityTestModule::class])
    abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun launcherActivity(): LauncherActivity

    @ContributesAndroidInjector(modules = [OnboardingActivityModule::class])
    abstract fun onboardingActivity(): OnboardingActivity
}
