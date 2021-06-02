package de.rki.coronawarnapp.greencertificate.ui.certificates

sealed class CertificatesFragmentEvents {

    data class OpenVaccinationRegistrationGraph(val registrationAcknowledged: Boolean) : CertificatesFragmentEvents()

    data class GoToVaccinationList(val personIdentifierCodeSha256: String) : CertificatesFragmentEvents()
}
