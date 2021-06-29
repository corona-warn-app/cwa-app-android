package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.LocalDate

data class VaccinatedPerson(
    internal val data: VaccinatedPersonData,
    private val valueSet: VaccinationValueSets?,
    val isUpdatingData: Boolean = false,
    val lastError: Throwable? = null,
) {
    val identifier: CertificatePersonIdentifier
        get() = data.identifier

    val vaccinationCertificates: Set<VaccinationCertificate> by lazy {
        data.vaccinations.map { it.toVaccinationCertificate(valueSet) }.toSet()
    }

    val vaccineName: String
        get() = vaccinationCertificates.first().vaccineTypeName

    val fullName: String
        get() = vaccinationCertificates.first().fullName

    val dateOfBirth: LocalDate
        get() = vaccinationCertificates.first().dateOfBirth

    val getMostRecentVaccinationCertificate: VaccinationCertificate
        get() = vaccinationCertificates.maxByOrNull { it.vaccinatedAt } ?: throw IllegalStateException(
            "Every Vaccinated Person needs to have at least one vaccinationCertificate"
        )

    fun getVaccinationStatus(nowUTC: Instant = Instant.now()): Status {
        val daysToImmunity = getDaysUntilImmunity(nowUTC) ?: return Status.INCOMPLETE

        return when {
            daysToImmunity <= 0 -> Status.IMMUNITY
            else -> Status.COMPLETE
        }
    }

    fun getDaysUntilImmunity(nowUTC: Instant = Instant.now()): Int? {
        val newestFullDose = vaccinationCertificates
            .filter { it.doseNumber == it.totalSeriesOfDoses }
            .maxByOrNull { it.vaccinatedAt }
            ?: return null

        val today = nowUTC
            .toLocalDateUserTz()

        return IMMUNITY_WAITING_DAYS - Days.daysBetween(newestFullDose.vaccinatedAt, today).days
    }

    enum class Status {
        INCOMPLETE,
        COMPLETE,
        IMMUNITY
    }

    companion object {
        private const val IMMUNITY_WAITING_DAYS = 15
    }
}
