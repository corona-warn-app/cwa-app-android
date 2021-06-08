package de.rki.coronawarnapp.covidcertificate.test.ui

sealed class CertificatesFragmentEvents {

    data class OpenVaccinationRegistrationGraph(val registrationAcknowledged: Boolean) : CertificatesFragmentEvents()

    data class GoToVaccinationList(val personIdentifierCodeSha256: String) : CertificatesFragmentEvents()

    data class GoToCovidCertificateDetailScreen(val identifier: String) : CertificatesFragmentEvents()
}
