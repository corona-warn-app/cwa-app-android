package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.recycle.Recyclable
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets

data class RecoveryCertificateWrapper(
    private val valueSets: VaccinationValueSets,
    private val container: RecoveryCertificateContainer,
    private val certificateState: CwaCovidCertificate.State,
) {

    val containerId: RecoveryCertificateContainerId get() = container.containerId

    val isUpdatingData = container.isUpdatingData

    val recycleInfo: Recyclable get() = container

    val recoveryCertificate: RecoveryCertificate by lazy {
        container.toRecoveryCertificate(
            valueSets,
            certificateState,
        )
    }
}
