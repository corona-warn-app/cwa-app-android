package de.rki.coronawarnapp.ui.onboarding

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.datadonation.analytics.ui.AnalyticsUIModule
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragment
import de.rki.coronawarnapp.release.NewReleaseInfoFragment
import de.rki.coronawarnapp.release.NewReleaseInfoFragmentModule

@Module
internal abstract class OnboardingActivityModule {

    // activity specific injection module for future dependencies

    // example:
    // @ContributesAndroidInjector
    // abstract fun onboardingFragment(): OnboardingFragmentFolder

    @ContributesAndroidInjector(modules = [OnboardingTracingModule::class])
    abstract fun onboardingScreen(): OnboardingTracingFragment

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

    @ContributesAndroidInjector(modules = [OnboardingLoadingModule::class])
    abstract fun onboardingLoadingScreen(): OnboardingLoadingFragment

    @ContributesAndroidInjector(modules = [NewReleaseInfoFragmentModule::class])
    abstract fun newReleaseInfoFragment(): NewReleaseInfoFragment

    @ContributesAndroidInjector(modules = [OnboardingDeltaInteroperabilityModule::class])
    abstract fun onboardingDeltaInteroperabilityFragment(): OnboardingDeltaInteroperabilityFragment

    @ContributesAndroidInjector(modules = [OnboardingDeltaAnalyticsModule::class])
    abstract fun onboardingDeltaAnalyticsFragment(): OnboardingDeltaAnalyticsFragment

    @ContributesAndroidInjector(modules = [OnboardingDeltaNotificationsModule::class])
    abstract fun onboardingDeltaNotificationsFragment(): OnboardingDeltaNotificationsFragment

    @ContributesAndroidInjector(modules = [OnboardingFragmentModule::class])
    abstract fun onboardingFragment(): OnboardingFragment
}
