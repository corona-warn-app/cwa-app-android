package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.repository.storage.PersonData
import de.rki.coronawarnapp.vaccination.core.server.VaccinationValueSet
import org.joda.time.Instant
import org.joda.time.LocalDate

data class VaccinatedPerson(
    internal val person: PersonData,
    private val valueSet: VaccinationValueSet?,
    val isUpdatingData: Boolean,
    val lastError: Throwable?,
) {
    val identifier: VaccinatedPersonIdentifier
        get() = person.identifier

    val lastUpdatedAt: Instant
        get() = person.proofs.maxOfOrNull { it.updatedAt } ?: person.vaccinations.maxOf { it.scannedAt }

    val vaccinationStatus: Status
        get() = if (proofCertificates.isNotEmpty()) Status.COMPLETE else Status.INCOMPLETE

    val vaccinationCertificates: Set<VaccinationCertificate>
        get() = person.vaccinations.map {
            it.toVaccinationCertificate(valueSet)
        }.toSet()

    val proofCertificates: Set<ProofCertificate>
        get() = person.proofs.map {
            it.toProofCertificate()
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

