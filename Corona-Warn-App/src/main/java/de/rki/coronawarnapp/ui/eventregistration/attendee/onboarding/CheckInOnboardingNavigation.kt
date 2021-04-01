package de.rki.coronawarnapp.ui.eventregistration.attendee.onboarding

sealed class CheckInOnboardingNavigation {
    object AcknowledgedNavigation : CheckInOnboardingNavigation()
    object DataProtectionNavigation : CheckInOnboardingNavigation()
}
