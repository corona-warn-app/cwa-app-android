package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import com.google.gson.annotations.SerializedName

data class RecoveryCertificateDTO(
    @SerializedName("data") val data: List<StoredRecoveryCertificateData>
)
