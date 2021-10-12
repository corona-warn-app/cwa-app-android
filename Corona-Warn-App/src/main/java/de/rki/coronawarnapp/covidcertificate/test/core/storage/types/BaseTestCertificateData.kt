package de.rki.coronawarnapp.covidcertificate.test.core.storage.types

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.reyclebin.common.Recyclable
import org.joda.time.Instant

/**
 * Common data for test certificates, independent of whether they were retrieved or scanned.
 */
sealed class BaseTestCertificateData: Recyclable {
    abstract val identifier: String
    abstract val registeredAt: Instant
    abstract val certificateReceivedAt: Instant?
    abstract val notifiedInvalidAt: Instant?
    abstract val lastSeenStateChange: CwaCovidCertificate.State?
    abstract val lastSeenStateChangeAt: Instant?
    abstract val testCertificateQrCode: String?
    abstract val certificateSeenByUser: Boolean
    abstract override val recycledAt: Instant?
}
