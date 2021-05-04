package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.repository.storage.PersonData
import de.rki.coronawarnapp.vaccination.core.server.VaccinationValueSet
import org.joda.time.LocalDate

data class VaccinatedPerson(
    internal val data: PersonData,
    private val valueSet: VaccinationValueSet?,
    val isUpdatingData: Boolean = false,
    val lastError: Throwable? = null,
) {
    val identifier: VaccinatedPersonIdentifier
        get() = data.identifier

    val vaccinationStatus: Status
        get() = if (proofCertificates.isNotEmpty()) Status.COMPLETE else Status.INCOMPLETE

    val vaccinationCertificates: Set<VaccinationCertificate>
        get() = data.vaccinations.map {
            it.toVaccinationCertificate(valueSet)
        }.toSet()

    val proofCertificates: Set<ProofCertificate>
        get() = data.proofs.map {
            it.toProofCertificate(valueSet)
        }.toSet()

    val firstName: String
        get() = vaccinationCertificates.first().firstName

    val lastName: String
        get() = vaccinationCertificates.first().lastName

    val dateOfBirth: LocalDate
        get() = vaccinationCertificates.first().dateOfBirth

    enum class Status {
        INCOMPLETE,
        COMPLETE
    }
}

