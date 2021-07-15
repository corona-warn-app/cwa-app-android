package de.rki.coronawarnapp.covidcertificate.common.repository

import android.os.Parcelable
import kotlin.reflect.KClass

/**
 * A unique identifier for a container holding a certificate in the CWA.
 * As there is a 1:1 match between container and certificates, it's also a unique ID for the certificate.
 * It's the ID you used pass around in the UI to find it/modify/delete it via a repository.
 * We can't use uniqueCertificateId because we also handle `TestCertificate`'s that have not been retrieved yet.
 */
sealed class CertificateContainerId : Parcelable {
    abstract val identifier: String

    val idType: KClass<out CertificateContainerId> = this::class

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CertificateContainerId) return false

        if (identifier != other.identifier) return false
        if (idType != other.idType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + idType.hashCode()
        return result
    }
}
