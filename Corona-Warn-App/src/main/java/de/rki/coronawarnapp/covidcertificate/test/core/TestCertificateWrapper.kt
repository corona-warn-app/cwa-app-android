package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainer
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets

data class TestCertificateWrapper(
    private val valueSets: TestCertificateValueSets,
    private val container: TestCertificateContainer
) {

    val identifier: TestCertificateIdentifier = container.identifier

    val isCertificateRetrievalPending = container.isCertificateRetrievalPending

    val isUpdatingData = container.isUpdatingData

    val registeredAt = container.registeredAt

    val testCertificate: TestCertificate? by lazy {
        container.toTestCertificate(valueSets, container)
    }
}
