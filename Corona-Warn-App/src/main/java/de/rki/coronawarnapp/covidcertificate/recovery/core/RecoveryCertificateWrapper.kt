package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainer
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.ValueSets

data class RecoveryCertificateWrapper(
    private val valueSets: ValueSets? = null,
    private val container: RecoveryCertificateContainer
) {

    val identifier: RecoveryCertificateIdentifier = container.identifier

    val isUpdatingData = container.isUpdatingData

    val testCertificate: RecoveryCertificate? by lazy {
        // TODO
        container.toRecoveryCertificate(null)
    }
}
