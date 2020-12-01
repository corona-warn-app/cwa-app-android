package de.rki.coronawarnapp.ui.submission.resultavailable

sealed class SubmissionTestResultAvailableEvents {
    object GoBack: SubmissionTestResultAvailableEvents()
    object GoConsent: SubmissionTestResultAvailableEvents()
    object Proceed: SubmissionTestResultAvailableEvents()
}
