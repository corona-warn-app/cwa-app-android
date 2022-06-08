package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import org.joda.time.LocalDate

interface VaccinationCertificate : CwaCovidCertificate {
    override val containerId: VaccinationCertificateContainerId

    val vaccinatedOn: LocalDate?
    val vaccinatedOnFormatted: String
    val vaccineManufacturer: String
    val doseNumber: Int
    val totalSeriesOfDoses: Int

    // To avoid further confusion:
    // vp
    val vaccineTypeName: String

    // mp
    val medicalProductName: String

    override val rawCertificate: VaccinationDccV1

    val isSeriesCompletingShot get() = rawCertificate.isSeriesCompletingShot

    companion object {
        const val iconComplete = R.drawable.ic_vaccination_immune
        const val iconIncomplete = R.drawable.ic_vaccination_incomplete
    }
}
