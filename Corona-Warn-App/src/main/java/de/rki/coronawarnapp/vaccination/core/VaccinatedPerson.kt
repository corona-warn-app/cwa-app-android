package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.repository.storage.PersonData
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import org.joda.time.Instant
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

    val firstName: String?
        get() = vaccinationCertificates.first().firstName

    val lastName: String
        get() = vaccinationCertificates.first().lastName

    val fullName: String
        get() = when {
            firstName == null -> lastName
            else -> "$firstName $lastName"
        }

    val dateOfBirth: LocalDate
        get() = vaccinationCertificates.first().dateOfBirth

    val isEligbleForProofCertificate: Boolean
        get() = data.isEligbleForProofCertificate

    val isProofCertificateCheckPending: Boolean
        get() = data.isPCRunPending

    val lastProofCheckAt: Instant
        get() = data.lastSuccessfulPCRunAt

    enum class Status {
        INCOMPLETE,
        COMPLETE
    }
}
