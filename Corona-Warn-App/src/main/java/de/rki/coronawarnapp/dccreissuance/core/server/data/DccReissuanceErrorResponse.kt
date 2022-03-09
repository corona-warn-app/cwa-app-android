package de.rki.coronawarnapp.dccreissuance.core.server.data

import com.google.gson.annotations.SerializedName

data class DccReissuanceErrorResponse(
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String
)
