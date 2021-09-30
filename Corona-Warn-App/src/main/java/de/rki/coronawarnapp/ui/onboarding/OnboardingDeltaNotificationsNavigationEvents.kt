package de.rki.coronawarnapp.ui.onboarding

sealed class OnboardingDeltaNotificationsNavigationEvents {
    object CloseScreen : OnboardingDeltaNotificationsNavigationEvents()
    object NavigateToOnboardingDeltaAnalyticsFragment : OnboardingDeltaNotificationsNavigationEvents()
}
