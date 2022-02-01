package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import org.joda.time.LocalDate

interface VaccinationCertificate : CwaCovidCertificate {
    override val containerId: VaccinationCertificateContainerId

    val vaccinatedOn: LocalDate
    val vaccinatedOnFormatted: String
    val targetDisease: String
    val vaccineManufacturer: String
    val doseNumber: Int
    val totalSeriesOfDoses: Int

    // To avoid further confusion:
    // vp
    val vaccineTypeName: String

    // mp
    val medicalProductName: String

    override val rawCertificate: VaccinationDccV1

    val isSeriesCompletingShot get() = doseNumber >= totalSeriesOfDoses
    val isBooster get() = doseNumber > totalSeriesOfDoses

    companion object {
        const val BIONTECH = "EU/1/20/1528"
        const val ASTRA = "EU/1/21/1529"
        const val MODERNA = "EU/1/20/1507"
        const val JOHNSON = "EU/1/20/1525"
        val ONE_SHOT_VACCINES get() = setOf(JOHNSON)
        val TWO_SHOT_VACCINES get() = setOf(BIONTECH, ASTRA, MODERNA)
    }
}
