package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

sealed class CheckInsConsentNavigation {
    object OpenCloseDialog : CheckInsConsentNavigation()
    object OpenSkipDialog : CheckInsConsentNavigation()
    object ToHomeFragment : CheckInsConsentNavigation()
    object ToSubmissionTestResultConsentGivenFragment : CheckInsConsentNavigation()
    object ToSubmissionResultReadyFragment : CheckInsConsentNavigation()
}
