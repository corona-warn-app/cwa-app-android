package de.rki.coronawarnapp.http.requests

import com.google.gson.annotations.SerializedName

data class RegistrationTokenRequest(
    @SerializedName("keyType")
    val keyType: String? = null,
    @SerializedName("key")
    val key: String? = null,
    @SerializedName("requestPadding")
    val requestPadding: String? = null
)
