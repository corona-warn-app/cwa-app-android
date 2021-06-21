package de.rki.coronawarnapp.covidcertificate.person.ui.details

import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId

sealed class PersonDetailsEvents
data class OpenVaccinationCertificateDetails(val containerId: VaccinationCertificateContainerId) : PersonDetailsEvents()
data class OpenTestCertificateDetails(val containerId: TestCertificateContainerId) : PersonDetailsEvents()
data class OpenRecoveryCertificateDetails(val containerId: RecoveryCertificateContainerId) : PersonDetailsEvents()
object Back : PersonDetailsEvents()
