package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationCertificateContainer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.reyclebin.common.Recyclable

data class VaccinationCertificateWrapper(
    private val valueSets: VaccinationValueSets,
    private val container: VaccinationCertificateContainer,
    private val certificateState: CwaCovidCertificate.State,
) {

    val containerId: VaccinationCertificateContainerId get() = container.containerId

    // TODO: test recycling
    val recycleInfo: Recyclable get() = container

    val vaccinationCertificate: VaccinationCertificate by lazy {
        container.toVaccinationCertificate(
            valueSets,
            certificateState
        )
    }
}
