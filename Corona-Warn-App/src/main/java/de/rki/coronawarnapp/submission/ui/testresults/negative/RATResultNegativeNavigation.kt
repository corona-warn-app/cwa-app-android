package de.rki.coronawarnapp.submission.ui.testresults.negative

import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId

sealed class RATResultNegativeNavigation {
    object Back : RATResultNegativeNavigation()
    object ShowDeleteWarning : RATResultNegativeNavigation()
    data class OpenTestCertificateDetails(val containerId: TestCertificateContainerId) : RATResultNegativeNavigation()
}
