package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import org.joda.time.LocalDate

interface VaccinationCertificate : CwaCovidCertificate {

    val vaccinatedAt: LocalDate

    val vaccineTypeName: String
    val vaccineManufacturer: String
    val medicalProductName: String

    val doseNumber: Int
    val totalSeriesOfDoses: Int

    val isFinalShot get() = doseNumber == totalSeriesOfDoses
}
