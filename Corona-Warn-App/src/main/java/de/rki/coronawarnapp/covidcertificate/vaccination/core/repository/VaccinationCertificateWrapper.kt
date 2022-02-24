package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainer
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.reyclebin.common.Recyclable
import org.joda.time.Instant

// Replacement for VaccinatedPerson

data class VaccinationCertificateWrapper(
    private val valueSets: VaccinationValueSets,
    private val container: VaccinationContainer,
    private val certificateState: CwaCovidCertificate.State,
) {

    val containerId: VaccinationCertificateContainerId get() = container.containerId

    val recycleInfo: Recyclable get() = container

    val vaccinationCertificate: VaccinationCertificate by lazy {
        container.toVaccinationCertificate(
            valueSets,
            certificateState
        )
    }
}
