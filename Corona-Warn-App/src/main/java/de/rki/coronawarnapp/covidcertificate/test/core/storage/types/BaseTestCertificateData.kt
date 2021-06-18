package de.rki.coronawarnapp.covidcertificate.test.core.storage.types

import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateIdentifier
import org.joda.time.Instant

/**
 * Common data for test certificates, idepdent of whether they were retrieved or scanned.
 */
sealed class BaseTestCertificateData {
    abstract val identifier: TestCertificateIdentifier
    abstract val registeredAt: Instant
    abstract val certificateReceivedAt: Instant?
    abstract val testCertificateQrCode: String?
}
