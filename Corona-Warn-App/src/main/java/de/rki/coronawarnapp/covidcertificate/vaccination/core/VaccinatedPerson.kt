package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets

// TODO: replace with wrapper
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

    fun findVaccination(containerId: VaccinationCertificateContainerId) = vaccinationContainers.find {
        it.containerId == containerId
    }

    val fullName: String?
        get() = allVaccinationCertificates.firstOrNull()?.fullName
}
