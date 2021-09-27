package de.rki.coronawarnapp.release

sealed class NewReleaseInfoNavigationEvents {
    object CloseScreen : NewReleaseInfoNavigationEvents()
    object NavigateToOnboardingDeltaNotificationManagementFragment : NewReleaseInfoNavigationEvents()
}
