package de.rki.coronawarnapp.ui.submission.yourconsent

sealed class SubmissionYourConsentEvents {
    object GoBack : SubmissionYourConsentEvents()
    object GoLegal : SubmissionYourConsentEvents()
}
