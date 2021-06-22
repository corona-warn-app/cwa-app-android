package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import org.joda.time.Duration
import org.joda.time.Instant

data class VaccinatedPerson(
    internal val data: VaccinatedPersonData,
    private val valueSet: VaccinationValueSets?,
    val isUpdatingData: Boolean = false,
    val lastError: Throwable? = null,
) {
    val identifier: CertificatePersonIdentifier
        get() = data.identifier

    val vaccinationContainers: Set<VaccinationContainer>
        get() = data.vaccinations

    val vaccinationCertificates: Set<VaccinationCertificate> by lazy {
        vaccinationContainers.map { it.toVaccinationCertificate(valueSet) }.toSet()
    }

    fun findVaccination(containerId: VaccinationCertificateContainerId) = vaccinationContainers.find {
        it.containerId == containerId
    }

    val vaccineName: String
        get() = vaccinationCertificates.first().vaccineTypeName

    val fullName: String
        get() = vaccinationCertificates.first().fullName

    val dateOfBirthFormatted: String
        get() = vaccinationCertificates.first().dateOfBirthFormatted

    val getMostRecentVaccinationCertificate: VaccinationCertificate
        get() = vaccinationCertificates.maxByOrNull { it.vaccinatedAtFormatted } ?: throw IllegalStateException(
            "Every Vaccinated Person needs to have at least one vaccinationCertificate"
        )

    fun getVaccinationStatus(nowUTC: Instant = Instant.now()): Status {
        val daysToImmunity = getTimeUntilImmunity(nowUTC)?.standardDays ?: return Status.INCOMPLETE

        return when {
            daysToImmunity < 0 -> Status.IMMUNITY
            else -> Status.COMPLETE
        }
    }

    fun getTimeUntilImmunity(nowUTC: Instant = Instant.now()): Duration? {
        val newestFullDose = vaccinationCertificates
            .filter { it.doseNumber == it.totalSeriesOfDoses }
            .maxByOrNull { it.vaccinatedAt }
            ?: return null

        val immunityAt = newestFullDose.vaccinatedAt
            .toDateTimeAtStartOfDay()
            .plus(IMMUNITY_WAITING_PERIOD)

        return Duration(nowUTC.toLocalDateUtc().toDateTimeAtStartOfDay(), immunityAt)
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
