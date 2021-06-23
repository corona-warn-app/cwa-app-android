package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import com.google.gson.annotations.SerializedName

data class StoredRecoveryCertificateData(
    @SerializedName("recoveryCertificateQrCode") override val recoveryCertificateQrCode: String?,
) : StoredRecoveryCertificate

interface StoredRecoveryCertificate {
    val recoveryCertificateQrCode: String
}
