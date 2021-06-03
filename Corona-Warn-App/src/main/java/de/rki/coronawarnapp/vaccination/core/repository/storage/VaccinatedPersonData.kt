package de.rki.coronawarnapp.vaccination.core.repository.storage

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.vaccination.core.CertificatePersonIdentifier

data class VaccinatedPersonData(
    @SerializedName("vaccinationData") val vaccinations: Set<VaccinationContainer> = emptySet()
) {
    val identifier: CertificatePersonIdentifier
        get() = vaccinations.first().personIdentifier
}
