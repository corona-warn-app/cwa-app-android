package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainer
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.LocalDate

data class VaccinatedPerson(
    internal val data: VaccinatedPersonData,
    private val valueSet: VaccinationValueSets?,
    private val certificateStates: Map<VaccinationCertificateContainerId, CwaCovidCertificate.State>,
    val isUpdatingData: Boolean = false,
    val lastError: Throwable? = null,
) {
    val identifier: CertificatePersonIdentifier
        get() = data.identifier

    val vaccinationContainers: Set<VaccinationContainer>
        get() = data.vaccinations

    val vaccinationCertificates: Set<VaccinationCertificate> by lazy {
        vaccinationContainers.map {
            it.toVaccinationCertificate(
                valueSet,
                certificateState = certificateStates.getValue(it.containerId)
            )
        }.toSet()
    }

    val hasBoosterNotification get() = false

    fun getDaysSinceLastVaccination(): Int {
        val today = Instant.now().toLocalDateUserTz()
        return Days.daysBetween(getNewestDoseVaccinatedOn(), today).days
    }

    val boosterRule: EvaluatedDccRule? = null

    fun findVaccination(containerId: VaccinationCertificateContainerId) = vaccinationContainers.find {
        it.containerId == containerId
    }

    val fullName: String
        get() = vaccinationCertificates.first().fullName

    val dateOfBirthFormatted: String
        get() = vaccinationCertificates.first().dateOfBirthFormatted

    fun getVaccinationStatus(nowUTC: Instant = Instant.now()): Status {
        if (boosterRule != null) return Status.BOOSTER_ELIGIBLE

        val daysToImmunity = getDaysUntilImmunity(nowUTC) ?: return Status.INCOMPLETE

        val isImmune = daysToImmunity <= 0 || isFirstVaccinationDoseAfterRecovery() || isBooster()
        return when {
            isImmune -> Status.IMMUNITY
            else -> Status.COMPLETE
        }
    }

    fun getDaysUntilImmunity(nowUTC: Instant = Instant.now()): Int? {
        val newestFullDose = getNewestFullDose() ?: return null
        val today = nowUTC
            .toLocalDateUserTz()

        return IMMUNITY_WAITING_DAYS - Days.daysBetween(newestFullDose.vaccinatedOn, today).days
    }

    private fun getNewestDoseVaccinatedOn(): LocalDate =
        vaccinationCertificates.maxOf { it.vaccinatedOn }

    private fun getNewestFullDose(): VaccinationCertificate? = vaccinationCertificates
        .filter { it.doseNumber >= it.totalSeriesOfDoses }
        .maxByOrNull { it.vaccinatedOn }

    private fun isFirstVaccinationDoseAfterRecovery(): Boolean {
        val vaccinationDetails = getNewestFullDose()?.rawCertificate?.vaccination
        return when (vaccinationDetails?.medicalProductId) {
            BIONTECH, ASTRA, MODERNA -> vaccinationDetails.doseNumber == 1
            else -> false
        }
    }

    private fun isBooster(): Boolean {
        val boosterVaccination = getNewestFullDose()?.rawCertificate?.vaccination
        return if (boosterVaccination != null) {
            when (boosterVaccination.medicalProductId) {
                BIONTECH, ASTRA, MODERNA -> boosterVaccination.doseNumber > 2
                else -> boosterVaccination.doseNumber > 1
            }
        } else false
    }

    enum class Status {
        INCOMPLETE,
        COMPLETE,
        IMMUNITY,
        BOOSTER_ELIGIBLE
    }

    companion object {
        private const val IMMUNITY_WAITING_DAYS = 15
        private const val BIONTECH = "EU/1/20/1528"
        private const val ASTRA = "EU/1/21/1529"
        private const val MODERNA = "EU/1/20/1507"
    }
}
