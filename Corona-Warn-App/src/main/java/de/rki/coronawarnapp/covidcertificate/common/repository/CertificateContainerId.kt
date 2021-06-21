package de.rki.coronawarnapp.covidcertificate.common.repository

import android.os.Parcelable

/**
 * A unique identifier for a container holding a certificate in the CWA.
 * As there is a 1:1 match between container and certificates, it's also a unique ID for the certificate.
 * It's the ID you used pass around in the UI to find it/modify/delete it via a repository.
 * We can't use uniqueCertificateId because we also handle `TestCertificate`'s that have not been retrieved yet.
 */
sealed class CertificateContainerId : Parcelable {
    abstract val identifier: String
}
