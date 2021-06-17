package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainer
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import org.joda.time.Instant

data class TestCertificateWrapper(
    private val valueSets: TestCertificateValueSets,
    private val container: TestCertificateContainer
) {

    val identifier: TestCertificateIdentifier get() = container.identifier

    val isCertificateRetrievalPending: Boolean get() = container.isCertificateRetrievalPending

    val isUpdatingData: Boolean get() = container.isUpdatingData

    val registeredAt: Instant get() = container.registeredAt

    val seenByUser: Boolean get() = container.certificateSeenByUser

    val registrationToken: String? get() = container.registrationToken

    val testCertificate: TestCertificate? by lazy {
        container.toTestCertificate(valueSets)
    }
}
