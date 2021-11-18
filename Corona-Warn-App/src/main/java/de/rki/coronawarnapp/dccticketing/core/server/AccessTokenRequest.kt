package de.rki.coronawarnapp.dccticketing.core.server

import com.google.gson.annotations.SerializedName

data class AccessTokenRequest(
    @SerializedName("service")
    val service: String,
    @SerializedName("pubKey")
    val pubKey: String
)
