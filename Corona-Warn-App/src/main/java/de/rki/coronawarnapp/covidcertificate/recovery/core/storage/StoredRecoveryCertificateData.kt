package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import org.joda.time.Instant

data class StoredRecoveryCertificateData(
    @SerializedName("recoveryCertificateQrCode") val recoveryCertificateQrCode: String,
    @SerializedName("notifiedInvalidAt") val notifiedInvalidAt: Instant? = null,
    @SerializedName("notifiedBlockedAt") val notifiedBlockedAt: Instant? = null,
    @SerializedName("notifiedRevokedAt") val notifiedRevokedAt: Instant? = null,
    @SerializedName("lastSeenStateChange") val lastSeenStateChange: State? = null,
    @SerializedName("lastSeenStateChangeAt") val lastSeenStateChangeAt: Instant? = null,
    @SerializedName("certificateSeenByUser") val certificateSeenByUser: Boolean = true,
    @SerializedName("recycledAt") val recycledAt: Instant? = null,
)
