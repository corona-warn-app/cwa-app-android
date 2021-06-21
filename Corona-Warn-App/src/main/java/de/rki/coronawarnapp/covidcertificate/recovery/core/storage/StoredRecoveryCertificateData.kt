package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import com.google.gson.annotations.SerializedName
import org.joda.time.Instant

data class StoredRecoveryCertificateData(
    @SerializedName("identifier") override val identifier: RecoveryCertificateIdentifier,
    @SerializedName("registeredAt") override val registeredAt: Instant,
    @SerializedName("recoveryCertificateQrCode") override val recoveryCertificateQrCode: String?,
) : StoredRecoveryCertificate {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoredRecoveryCertificateData

        if (identifier != other.identifier) return false

        return true
    }
}

interface StoredRecoveryCertificate {
    val identifier: RecoveryCertificateIdentifier
    val registeredAt: Instant
    val recoveryCertificateQrCode: String?
}
