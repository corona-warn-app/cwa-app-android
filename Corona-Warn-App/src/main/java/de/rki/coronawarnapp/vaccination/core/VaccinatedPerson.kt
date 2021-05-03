package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationContainer
import de.rki.coronawarnapp.vaccination.core.server.VaccinationValueSet
import org.joda.time.Instant
import org.joda.time.LocalDate

data class VaccinatedPerson(
    private val valueSet: VaccinationValueSet,
    val vaccinationCertificates: Set<VaccinationContainer>,
    val proofCertificates: Set<ProofCertificate>,
    @Transient val isUpdatingData: Boolean,
    @Transient val lastError: Throwable?,
) {
    val identifier: VaccinatedPersonIdentifier
        get() = vaccinationCertificates.first().personIdentifier

    val lastUpdatedAt: Instant
        get() = proofCertificates.maxOfOrNull { it.updatedAt } ?: vaccinationCertificates.maxOf { it.scannedAt }

    val firstName: String
        get() = vaccinationCertificates.first().firstName

    val lastName: String
        get() = vaccinationCertificates.first().lastName

    val dateOfBirth: LocalDate
        get() = vaccinationCertificates.first().dateOfBirth

    val vaccinationStatus: Status
        get() = if (proofCertificates.isNotEmpty()) Status.COMPLETE else Status.INCOMPLETE

    enum class Status {
        INCOMPLETE,
        COMPLETE
    }
}

typealias VaccinatedPersonIdentifier = String
