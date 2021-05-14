package de.rki.coronawarnapp.vaccination.core.repository.storage

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier

data class VaccinatedPersonData(
    @SerializedName("vaccinationData") val vaccinations: Set<VaccinationContainer>
) {
    val identifier: VaccinatedPersonIdentifier
        get() = vaccinations.first().personIdentifier
}
