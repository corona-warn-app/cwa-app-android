package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.CertificatePersonIdentifier

data class VaccinatedPersonData(
    @SerializedName("vaccinationData") val vaccinations: Set<VaccinationContainer> = emptySet()
) {
    val identifier: CertificatePersonIdentifier
        get() = vaccinations.first().personIdentifier
}
