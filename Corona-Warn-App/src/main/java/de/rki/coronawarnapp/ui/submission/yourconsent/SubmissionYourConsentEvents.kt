package de.rki.coronawarnapp.ui.submission.yourconsent

sealed class SubmissionYourConsentEvents {
    object GoBack : SubmissionYourConsentEvents()
    object SwitchConsent : SubmissionYourConsentEvents()
    object GoLegal : SubmissionYourConsentEvents()
}
