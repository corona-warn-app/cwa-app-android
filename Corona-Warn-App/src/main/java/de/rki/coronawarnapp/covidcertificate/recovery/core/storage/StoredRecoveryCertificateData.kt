package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import com.google.gson.annotations.SerializedName
import org.joda.time.Instant
import java.util.UUID

data class StoredRecoveryCertificateData(
    @SerializedName("uuid") val uuid: UUID = UUID.randomUUID(),
    @SerializedName("identifier") override val identifier: RecoveryCertificateIdentifier,
    @SerializedName("registeredAt") override val registeredAt: Instant,
    @SerializedName("recoveryCertificateQrCode") override val recoveryCertificateQrCode: String?,
) : StoredRecoveryCertificate

interface StoredRecoveryCertificate {
    val identifier: RecoveryCertificateIdentifier
    val registeredAt: Instant
    val recoveryCertificateQrCode: String?
}
