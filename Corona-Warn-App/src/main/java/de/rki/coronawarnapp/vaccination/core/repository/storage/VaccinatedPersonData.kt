package de.rki.coronawarnapp.vaccination.core.repository.storage

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import org.joda.time.Instant

data class VaccinatedPersonData(
    @SerializedName("vaccinationData") val vaccinations: Set<VaccinationContainer>,
    @SerializedName("proofData") val proofs: Set<ProofContainer>,
    @SerializedName("lastSuccessfulProofCertificateRun") val lastSuccessfulPCRunAt: Instant = Instant.EPOCH,
    @SerializedName("proofCertificateRunPending") val isPCRunPending: Boolean = false,
) {
    val identifier: VaccinatedPersonIdentifier
        get() = vaccinations.first().personIdentifier

    val isEligbleForProofCertificate: Boolean
        get() = vaccinations.any { it.isEligbleForProofCertificate }
}
