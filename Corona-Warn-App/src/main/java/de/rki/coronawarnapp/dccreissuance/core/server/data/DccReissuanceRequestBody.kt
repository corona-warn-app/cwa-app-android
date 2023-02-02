package de.rki.coronawarnapp.dccreissuance.core.server.data

import com.fasterxml.jackson.annotation.JsonProperty

data class DccReissuanceRequestBody(
    @JsonProperty("action") val action: String,
    @JsonProperty("certificates") val certificates: List<String>
)
