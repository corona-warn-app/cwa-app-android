package de.rki.coronawarnapp.covidcertificate.person.ui.details

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade

sealed class PersonDetailsEvents

data class OpenVaccinationCertificateDetails(
    val containerId: VaccinationCertificateContainerId,
    val colorShade: PersonColorShade
) : PersonDetailsEvents()

data class OpenTestCertificateDetails(
    val containerId: TestCertificateContainerId,
    val colorShade: PersonColorShade
) : PersonDetailsEvents()

data class OpenRecoveryCertificateDetails(
    val containerId: RecoveryCertificateContainerId,
    val colorShade: PersonColorShade
) : PersonDetailsEvents()

data class ValidationStart(val containerId: CertificateContainerId) : PersonDetailsEvents()
data class ShowErrorDialog(val error: Throwable) : PersonDetailsEvents()
data class OpenBoosterInfoDetails(val groupKey: String) : PersonDetailsEvents()
data class OpenCertificateReissuanceConsent(val groupKey: String) : PersonDetailsEvents()
data class RecycleCertificate(
    val cwaCovidCertificate: CwaCovidCertificate,
    val position: Int
) : PersonDetailsEvents()

object Back : PersonDetailsEvents()
object OpenCovPassInfo : PersonDetailsEvents()
