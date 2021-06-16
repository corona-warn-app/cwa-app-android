package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainer
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets

data class TestCertificateWrapper(
    private val valueSets: TestCertificateValueSets,
    private val container: TestCertificateContainer
) {

    val identifier: TestCertificateIdentifier get() = container.identifier

    val isCertificateRetrievalPending get() = container.isCertificateRetrievalPending

    val isUpdatingData get() = container.isUpdatingData

    val registeredAt get() = container.registeredAt

    val testCertificate: TestCertificate? by lazy {
        container.toTestCertificate(valueSets)
    }
}
