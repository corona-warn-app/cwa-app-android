package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import java.time.Instant

data class StoredRecoveryCertificateData(
    @JsonProperty("recoveryCertificateQrCode") val recoveryCertificateQrCode: String,
    @JsonProperty("notifiedInvalidAt") val notifiedInvalidAt: Instant? = null,
    @JsonProperty("notifiedBlockedAt") val notifiedBlockedAt: Instant? = null,
    @JsonProperty("notifiedRevokedAt") val notifiedRevokedAt: Instant? = null,
    @JsonProperty("lastSeenStateChange") val lastSeenStateChange: State? = null,
    @JsonProperty("lastSeenStateChangeAt") val lastSeenStateChangeAt: Instant? = null,
    @JsonProperty("certificateSeenByUser") val certificateSeenByUser: Boolean = true,
    @JsonProperty("recycledAt") val recycledAt: Instant? = null,
)
