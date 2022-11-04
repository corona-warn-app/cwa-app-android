package de.rki.coronawarnapp.srs.core.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SrsAuthorizationResponse(
    @JsonProperty("expirationDate") val expirationDate: String? = null,
    @JsonProperty("errorCode") val errorCode: String? = null,
)
