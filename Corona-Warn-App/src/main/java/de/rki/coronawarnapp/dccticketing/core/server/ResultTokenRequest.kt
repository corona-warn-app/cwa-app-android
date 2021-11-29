package de.rki.coronawarnapp.dccticketing.core.server

import com.google.gson.annotations.SerializedName

data class ResultTokenRequest(
    @SerializedName("kid")
    val kid: String,
    @SerializedName("dcc")
    val dcc: String,
    @SerializedName("sig")
    val sig: String,
    @SerializedName("encKey")
    val encKey: String,
    @SerializedName("encScheme")
    val encScheme: String,
    @SerializedName("sigAlg")
    val sigAlg: String
)
