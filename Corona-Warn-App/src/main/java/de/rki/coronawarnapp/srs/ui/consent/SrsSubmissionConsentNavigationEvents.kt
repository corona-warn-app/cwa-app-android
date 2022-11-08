package de.rki.coronawarnapp.srs.ui.consent

sealed class SrsSubmissionConsentNavigationEvents {
    object NavigateToMainScreen : SrsSubmissionConsentNavigationEvents()
    object NavigateToDataPrivacy : SrsSubmissionConsentNavigationEvents()
    object NavigateToTestType : SrsSubmissionConsentNavigationEvents()
    object NavigateToShareCheckins : SrsSubmissionConsentNavigationEvents()
    object NavigateToShareSymptoms : SrsSubmissionConsentNavigationEvents()
}
