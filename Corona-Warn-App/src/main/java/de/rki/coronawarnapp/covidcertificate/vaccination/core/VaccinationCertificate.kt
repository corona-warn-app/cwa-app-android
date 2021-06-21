package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import org.joda.time.LocalDate

interface VaccinationCertificate : CwaCovidCertificate {
    override val containerId: VaccinationCertificateContainerId

    val vaccinatedAt: LocalDate
    val targetDisease: String
    val vaccineTypeName: String
    val vaccineManufacturer: String
    val medicalProductName: String
    val doseNumber: Int
    val totalSeriesOfDoses: Int

    val isFinalShot get() = doseNumber == totalSeriesOfDoses
}
