package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import org.joda.time.Instant

/**
 * For use with the UI
 */
interface CwaCovidCertificate {
    // Header
    val issuer: String
    val issuedAt: Instant
    val expiresAt: Instant
    val qrCode: QrCodeString
    val firstName: String?
    val lastName: String
    val fullName: String
    val dateOfBirthFormatted: String
    val personIdentifier: CertificatePersonIdentifier
    val certificateIssuer: String
    val certificateCountry: String
    val certificateId: String

    /**
     * The ID of the container holding this certificate in the CWA.
     */
    val containerId: CertificateContainerId

    val rawCertificate: DccV1.MetaData

    val dccData: DccData<out DccV1.MetaData>
}
