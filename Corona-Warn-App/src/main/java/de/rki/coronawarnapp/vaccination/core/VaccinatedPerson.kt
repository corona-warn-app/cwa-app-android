package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import org.joda.time.LocalDate

data class VaccinatedPerson(
    internal val data: VaccinatedPersonData,
    private val valueSet: VaccinationValueSet?,
    val isUpdatingData: Boolean = false,
    val lastError: Throwable? = null,
) {
    val identifier: VaccinatedPersonIdentifier
        get() = data.identifier

    val vaccinationCertificates: Set<VaccinationCertificate>
        get() = data.vaccinations.map {
            it.toVaccinationCertificate(valueSet)
        }.toSet()

    @Deprecated("Will be removed in PR #3160")
    val vaccinationStatus: Status
        get() = Status.INCOMPLETE

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

    enum class Status {
        INCOMPLETE,
        COMPLETE
    }
}
