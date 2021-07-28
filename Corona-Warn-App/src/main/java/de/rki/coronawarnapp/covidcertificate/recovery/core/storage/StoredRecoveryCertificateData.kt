package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import com.google.gson.annotations.SerializedName
import org.joda.time.Instant

data class StoredRecoveryCertificateData(
    @SerializedName("recoveryCertificateQrCode") val recoveryCertificateQrCode: String,
    @SerializedName("notifiedExpiresSoonAt") val notifiedExpiresSoonAt: Instant? = null,
    @SerializedName("notifiedExpiredAt") val notifiedExpiredAt: Instant? = null,
)
