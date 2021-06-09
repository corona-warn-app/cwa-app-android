package de.rki.coronawarnapp.covidcertificate.test.ui

import de.rki.coronawarnapp.coronatest.type.common.TestCertificateIdentifier

sealed class CertificatesFragmentEvents {

    data class OpenVaccinationRegistrationGraph(val registrationAcknowledged: Boolean) : CertificatesFragmentEvents()

    data class GoToVaccinationList(val personIdentifierCodeSha256: String) : CertificatesFragmentEvents()

    data class GoToCovidCertificateDetailScreen(val identifier: String) : CertificatesFragmentEvents()

    data class ShowRefreshErrorCertificateDialog(val error: Exception) : CertificatesFragmentEvents()

    data class ShowDeleteErrorCertificateDialog(val identifier: TestCertificateIdentifier) : CertificatesFragmentEvents()
}
