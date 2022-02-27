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
    val sampleCollectedAt: Instant?
    val sampleCollectedAtFormatted: String
    val testCenter: String?
    val registeredAt: Instant
    val isUpdatingData: Boolean
    val isCertificateRetrievalPending: Boolean

    val isPCRTestCertificate: Boolean get() = rawCertificate.test.testType == PCR_TEST
    val isRapidAntigenTestCertificate: Boolean get() = rawCertificate.test.testType == RAT_TEST

    /**
     * Not supported by this type of certificate (at the moment)
     */
    override val notifiedExpiredAt: Instant?
        get() = null
    override val notifiedExpiresSoonAt: Instant?
        get() = null

    override val rawCertificate: TestDccV1

    companion object {
        const val PCR_TEST = "LP6464-4"
        const val RAT_TEST = "LP217198-3"
    }
}
