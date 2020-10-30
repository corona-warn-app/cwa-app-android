package de.rki.coronawarnapp.ui.submission.viewmodel

import de.rki.coronawarnapp.submission.Symptoms

sealed class SubmissionNavigationEvents {
    object NavigateToContact : SubmissionNavigationEvents()
    object NavigateToDispatcher : SubmissionNavigationEvents()
    object NavigateToSubmissionDone : SubmissionNavigationEvents()
    object NavigateToSubmissionIntro : SubmissionNavigationEvents()
    object NavigateToQRCodeScan : SubmissionNavigationEvents()

    data class NavigateToResultPositiveOtherWarning(
        val symptoms: Symptoms
    ) : SubmissionNavigationEvents()

    object NavigateToSymptomSubmission : SubmissionNavigationEvents()
    data class NavigateToSymptomCalendar(
        val symptomIndication: Symptoms.Indication
    ) : SubmissionNavigationEvents()

    object NavigateToSymptomIntroduction : SubmissionNavigationEvents()
    object NavigateToTAN : SubmissionNavigationEvents()
    object NavigateToTestResult : SubmissionNavigationEvents()
    object NavigateToQRInfo : SubmissionNavigationEvents()
    object NavigateToMainActivity : SubmissionNavigationEvents()
    object ShowCancelDialog : SubmissionNavigationEvents()
}
