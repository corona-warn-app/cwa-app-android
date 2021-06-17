package de.rki.coronawarnapp.covidcertificate.person.ui.details

sealed class PersonDetailsEvents
data class OpenVaccinationCertificateDetails(val certificateId: String) : PersonDetailsEvents()
data class OpenTestCertificateDetails(val certificateId: String) : PersonDetailsEvents()
data class OpenRecoveryCertificateDetails(val certificateId: String) : PersonDetailsEvents()
