package de.rki.coronawarnapp.vaccination.core

import org.joda.time.Instant
import org.joda.time.LocalDate

data class VaccinatedPerson(
    val vaccinationCertificates: Set<VaccinationCertificate>,
    val proofCertificates: Set<ProofCertificate>,
    val isRefreshing: Boolean,
    val lastUpdatedAt: Instant,
) {
    val identifier: VaccinatedPersonIdentifier = ""

    val firstName: String
        get() = ""

    val lastName: String
        get() = ""

    val dateOfBirth: LocalDate
        get() = LocalDate.now()

    val vaccinationStatus: Status
        get() = if (proofCertificates.isNotEmpty()) Status.COMPLETE else Status.INCOMPLETE

    enum class Status {
        INCOMPLETE,
        COMPLETE
    }
}

typealias VaccinatedPersonIdentifier = String
