package de.rki.coronawarnapp.dccreissuance.core.server.data

import com.fasterxml.jackson.annotation.JsonProperty

data class DccReissuanceErrorResponse(
    @JsonProperty("error") val error: String,
    @JsonProperty("message") val message: String
)
