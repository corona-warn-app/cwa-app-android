package de.rki.coronawarnapp.ui.onboarding

sealed class OnboardingNavigationEvents {
    object NavigateToOnboardingPrivacy: OnboardingNavigationEvents()
    object NavigateToEasyLanguageUrl: OnboardingNavigationEvents()
    object NavigateToOnboardingTracing: OnboardingNavigationEvents()
    object NavigateToOnboardingFragment: OnboardingNavigationEvents()
    object NavigateToOnboardingTest: OnboardingNavigationEvents()
    object ShowCancelDialog: OnboardingNavigationEvents()
    object NavigateToOnboardingNotifications: OnboardingNavigationEvents()
    object NavigateToMainActivity: OnboardingNavigationEvents()
}
