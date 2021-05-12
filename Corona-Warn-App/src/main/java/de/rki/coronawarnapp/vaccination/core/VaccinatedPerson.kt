package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate

data class VaccinatedPerson(
    internal val data: VaccinatedPersonData,
    private val valueSet: VaccinationValueSet?,
    val isUpdatingData: Boolean = false,
    val lastError: Throwable? = null,
) {
    val identifier: VaccinatedPersonIdentifier
        get() = data.identifier

    val vaccinationCertificates: Set<VaccinationCertificate> by lazy {
        data.vaccinations.map { it.toVaccinationCertificate(valueSet) }.toSet()
    }

    val proofCertificates: Set<ProofCertificate> by lazy {
        data.proofs.map { it.toProofCertificate(valueSet) }.toSet()
    }

    val vaccineName: String
        get() = vaccinationCertificates.first().vaccineName

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

    fun getVaccinationStatus(nowUTC: Instant = Instant.now()): Status {
        val newestFullDose = vaccinationCertificates
            .filter { it.doseNumber == it.totalSeriesOfDoses }
            .maxByOrNull { it.vaccinatedAt }
            ?: return Status.INCOMPLETE

        val daysAgo = Duration(newestFullDose.vaccinatedAt.toDateTimeAtStartOfDay(), nowUTC).standardDays

        return when {
            daysAgo >= IMMUNITY_WAITING_PERIOD.standardDays -> Status.IMMUNITY
            else -> Status.COMPLETE
        }
    }

    enum class Status {
        INCOMPLETE,
        COMPLETE,
        IMMUNITY
    }

    companion object {
        private val IMMUNITY_WAITING_PERIOD = Duration.standardDays(14)
    }
}
