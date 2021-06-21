package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import com.google.gson.annotations.SerializedName
import org.joda.time.Instant

data class StoredRecoveryCertificateData(
    @SerializedName("identifier") override val identifier: String,
    @SerializedName("registeredAt") override val registeredAt: Instant,
    @SerializedName("recoveryCertificateQrCode") override val recoveryCertificateQrCode: String?,
) : StoredRecoveryCertificate

interface StoredRecoveryCertificate {
    val identifier: String
    val registeredAt: Instant
    val recoveryCertificateQrCode: String?
}
