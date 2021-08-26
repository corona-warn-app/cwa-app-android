package de.rki.coronawarnapp.ui.submission.testresult.negative

import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId

sealed class SubmissionTestResultNegativeNavigation {
    object Back : SubmissionTestResultNegativeNavigation()
    data class OpenTestCertificateDetails(val containerId: TestCertificateContainerId) :
        SubmissionTestResultNegativeNavigation()
}
