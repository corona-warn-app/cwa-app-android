package de.rki.coronawarnapp.dccreissuance.core.server.data

import com.google.gson.annotations.SerializedName

// TODO: Ask Max for clarification if it is error or errorCode
data class DccReissuanceErrorResponse(
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String
)
