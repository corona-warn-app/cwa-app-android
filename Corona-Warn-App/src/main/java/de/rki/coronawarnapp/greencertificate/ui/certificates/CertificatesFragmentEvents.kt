package de.rki.coronawarnapp.greencertificate.ui.certificates

import de.rki.coronawarnapp.coronatest.type.TestCertificateIdentifier

sealed class CertificatesFragmentEvents {

    data class OpenVaccinationRegistrationGraph(val registrationAcknowledged: Boolean) : CertificatesFragmentEvents()

    data class GoToVaccinationList(val personIdentifierCodeSha256: String) : CertificatesFragmentEvents()

    data class ShowRefreshErrorCertificateDialog(val error: Exception) : CertificatesFragmentEvents()

    data class ShowDeleteErrorCertificateDialog(val identifier: TestCertificateIdentifier) : CertificatesFragmentEvents()
}
