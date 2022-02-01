package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate.Companion.ASTRA
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate.Companion.BIONTECH
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate.Companion.MODERNA
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainer
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.LocalDate

data class VaccinatedPerson(
    internal val data: VaccinatedPersonData,
    private val valueSet: VaccinationValueSets?,
    private val certificateStates: Map<VaccinationCertificateContainerId, CwaCovidCertificate.State>,
) {
    val identifier: CertificatePersonIdentifier
        get() = data.identifier

    val vaccinationContainers: Set<VaccinationContainer>
        get() = data.vaccinations

    val vaccinationCertificates: Set<VaccinationCertificate> by lazy {
        vaccinationContainers
            .filter { it.isNotRecycled }
            .mapToVaccinationCertificateSet()
    }

    val recycledVaccinationCertificates: Set<VaccinationCertificate> by lazy {
        vaccinationContainers
            .filter { it.isRecycled }
            .mapToVaccinationCertificateSet(state = CwaCovidCertificate.State.Recycled)
    }

    private val allVaccinationCertificates: Set<VaccinationCertificate> by lazy {
        vaccinationContainers.mapToVaccinationCertificateSet()
    }

    private fun Collection<VaccinationContainer>.mapToVaccinationCertificateSet(
        state: CwaCovidCertificate.State? = null
    ): Set<VaccinationCertificate> = map {
        it.toVaccinationCertificate(
            valueSet,
            certificateState = state ?: certificateStates.getValue(it.containerId)
        )
    }.toSet()

    val hasBoosterNotification: Boolean
        get() = data.boosterRule?.identifier != data.lastSeenBoosterRuleIdentifier

    @Throws(NoSuchElementException::class)
    fun getDaysSinceLastVaccination(): Int {
        val today = Instant.now().toLocalDateUserTz()
        return Days.daysBetween(getNewestDoseVaccinatedOn(), today).days
    }

    val boosterRule: DccValidationRule?
        get() = data.boosterRule

    fun findVaccination(containerId: VaccinationCertificateContainerId) = vaccinationContainers.find {
        it.containerId == containerId
    }

    val fullName: String?
        get() = allVaccinationCertificates.firstOrNull()?.fullName

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
        val today = nowUTC.toLocalDateUserTz()
        return if (isSeriesCompletingOverTwoWeeks(nowUTC.toLocalDateUtc())) 0
        else IMMUNITY_WAITING_DAYS - Days.daysBetween(newestFullDose.vaccinatedOn, today).days
    }

    @Throws(NoSuchElementException::class)
    private fun getNewestDoseVaccinatedOn(): LocalDate =
        vaccinationCertificates.maxOf { it.vaccinatedOn }

    private fun isSeriesCompletingOverTwoWeeks(today: LocalDate): Boolean {
        return when {
            vaccinationCertificates.isEmpty() -> false
            vaccinationCertificates.any { it.isBooster } -> true
            else -> vaccinationCertificates.any {
                it.isSeriesCompletingShot && Days.daysBetween(
                    it.rawCertificate.vaccination.vaccinatedOn,
                    today
                ).days > 14
            }
        }
    }

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
    }
}
