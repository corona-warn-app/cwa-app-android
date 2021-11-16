package de.rki.coronawarnapp.dccticketing.core.server

import com.fasterxml.jackson.annotation.JsonProperty

data class AccessTokenRequest(
    @JsonProperty("service")
    val service: String,
    @JsonProperty("pubKey")
    val pubKey: String
)
