package de.rki.coronawarnapp.ui.onboarding

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.datadonation.analytics.ui.AnalyticsUIModule
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragment

@Module
internal abstract class OnboardingActivityModule {

    // activity specific injection module for future dependencies

    // example:
    // @ContributesAndroidInjector
    // abstract fun onboardingFragment(): OnboardingFragmentFolder

    @ContributesAndroidInjector(modules = [OnboardingTracingModule::class])
    abstract fun onboardingScreen(): OnboardingTracingFragment
    @ContributesAndroidInjector(modules = [OnboardingFragmentModule::class])
    abstract fun onboardingFragment(): OnboardingFragment
    @ContributesAndroidInjector(modules = [OnboardingPrivacyModule::class])
    abstract fun onboardingPrivacyFragment(): OnboardingPrivacyFragment
    @ContributesAndroidInjector(modules = [OnboardingTestModule::class])
    abstract fun onboardingTestFragment(): OnboardingTestFragment
    @ContributesAndroidInjector(modules = [OnboardingNotificationsModule::class])
    abstract fun onboardingNotificationsFragment(): OnboardingNotificationsFragment
    @ContributesAndroidInjector(modules = [OnboardingAnalyticsModule::class])
    abstract fun onboardingAnalyticsFragment(): OnboardingAnalyticsFragment
    @ContributesAndroidInjector(modules = [AnalyticsUIModule::class])
    abstract fun ppaUserInfoSelection(): AnalyticsUserInputFragment
}
