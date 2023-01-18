package de.rki.coronawarnapp.dccticketing.core.server

import com.fasterxml.jackson.annotation.JsonProperty

data class ResultTokenRequest(
    @JsonProperty("kid")
    val kid: String,
    @JsonProperty("dcc")
    val dcc: String,
    @JsonProperty("sig")
    val sig: String,
    @JsonProperty("encKey")
    val encKey: String,
    @JsonProperty("encScheme")
    val encScheme: String,
    @JsonProperty("sigAlg")
    val sigAlg: String
)
