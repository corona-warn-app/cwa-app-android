package de.rki.coronawarnapp.ui.presencetracing.attendee.onboarding

sealed class CheckInOnboardingNavigation {
    object AcknowledgedNavigation : CheckInOnboardingNavigation()
    object DataProtectionNavigation : CheckInOnboardingNavigation()
    object SkipOnboardingInfo : CheckInOnboardingNavigation()
}
