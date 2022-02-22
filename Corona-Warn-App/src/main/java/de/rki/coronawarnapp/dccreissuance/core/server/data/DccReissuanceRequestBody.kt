package de.rki.coronawarnapp.dccreissuance.core.server.data

import com.google.gson.annotations.SerializedName

data class DccReissuanceRequestBody(
    @SerializedName("action") val action: String,
    @SerializedName("certificates") val certificates: List<String>
)
