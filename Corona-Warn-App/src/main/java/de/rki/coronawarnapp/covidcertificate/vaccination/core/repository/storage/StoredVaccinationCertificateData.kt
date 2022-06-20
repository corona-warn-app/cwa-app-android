package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import org.joda.time.Instant

data class StoredVaccinationCertificateData(
    @SerializedName("vaccinationQrCode") val vaccinationQrCode: QrCodeString,
    @SerializedName("scannedAt") val scannedAt: Instant,
    @SerializedName("notifiedInvalidAt") val notifiedInvalidAt: Instant? = null,
    @SerializedName("notifiedBlockedAt") val notifiedBlockedAt: Instant? = null,
    @SerializedName("notifiedRevokedAt") val notifiedRevokedAt: Instant? = null,
    @SerializedName("lastSeenStateChange") val lastSeenStateChange: CwaCovidCertificate.State? = null,
    @SerializedName("lastSeenStateChangeAt") val lastSeenStateChangeAt: Instant? = null,
    @SerializedName("certificateSeenByUser") val certificateSeenByUser: Boolean = true,
    @SerializedName("recycledAt") val recycledAt: Instant? = null,
)
