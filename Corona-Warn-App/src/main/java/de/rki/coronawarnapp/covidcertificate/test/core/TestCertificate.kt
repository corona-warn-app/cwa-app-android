package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import org.joda.time.Instant

interface TestCertificate : CwaCovidCertificate {
    override val containerId: TestCertificateContainerId

    /**
     * Disease or agent targeted (required)
     */
    val targetName: String
    val testType: String
    val testResult: String

    /**
     * NAA Test Name (only for PCR tests, but not required)
     */
    val testName: String?

    /**
     * RAT Test name and manufacturer (only for RAT tests, but not required)
     */
    val testNameAndManufacturer: String?
    val sampleCollectedAt: Instant
    val testCenter: String?
    val registeredAt: Instant
    val isUpdatingData: Boolean
    val isCertificateRetrievalPending: Boolean

    override val rawCertificate: TestDccV1
}
