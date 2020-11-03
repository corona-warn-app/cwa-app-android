package de.rki.coronawarnapp.ui.submission.viewmodel

sealed class SubmissionNavigationEvents {
    object NavigateToContact : SubmissionNavigationEvents()
    object NavigateToDispatcher : SubmissionNavigationEvents()
    object NavigateToSubmissionDone : SubmissionNavigationEvents()
    object NavigateToSubmissionIntro : SubmissionNavigationEvents()
    object NavigateToQRCodeScan : SubmissionNavigationEvents()
    object NavigateToResultPositiveOtherWarning : SubmissionNavigationEvents()
    object NavigateToSymptomSubmission : SubmissionNavigationEvents()
    object NavigateToSymptomCalendar : SubmissionNavigationEvents()
    object NavigateToSymptomIntroduction : SubmissionNavigationEvents()
    object NavigateToTAN : SubmissionNavigationEvents()
    object NavigateToTestResult : SubmissionNavigationEvents()
    object NavigateToQRInfo : SubmissionNavigationEvents()
    object NavigateToMainActivity : SubmissionNavigationEvents()
    object ShowCancelDialog : SubmissionNavigationEvents()
}
