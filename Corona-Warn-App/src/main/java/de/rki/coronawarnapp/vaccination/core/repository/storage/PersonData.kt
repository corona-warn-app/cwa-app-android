package de.rki.coronawarnapp.vaccination.core.repository.storage

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier

data class PersonData(
    @SerializedName("vaccinationData") internal val vaccinations: Set<VaccinationContainer>,
    @SerializedName("proofData") internal val proofs: Set<ProofContainer>,
) {
    val identifier: VaccinatedPersonIdentifier
        get() = vaccinations.first().personIdentifier
}
