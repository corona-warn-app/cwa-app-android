package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import java.time.Instant

data class StoredVaccinationCertificateData(
    @JsonProperty("vaccinationQrCode") val vaccinationQrCode: QrCodeString,
    @JsonProperty("scannedAt") val scannedAt: Instant,
    @JsonProperty("notifiedInvalidAt") val notifiedInvalidAt: Instant? = null,
    @JsonProperty("notifiedBlockedAt") val notifiedBlockedAt: Instant? = null,
    @JsonProperty("notifiedRevokedAt") val notifiedRevokedAt: Instant? = null,
    @JsonProperty("lastSeenStateChange") val lastSeenStateChange: CwaCovidCertificate.State? = null,
    @JsonProperty("lastSeenStateChangeAt") val lastSeenStateChangeAt: Instant? = null,
    @JsonProperty("certificateSeenByUser") val certificateSeenByUser: Boolean = true,
    @JsonProperty("recycledAt") val recycledAt: Instant? = null,
)
