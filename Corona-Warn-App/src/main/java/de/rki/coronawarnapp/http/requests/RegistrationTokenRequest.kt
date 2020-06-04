package de.rki.coronawarnapp.http.requests

import com.google.gson.annotations.SerializedName

data class RegistrationTokenRequest(
    @SerializedName("keyType")
    val keyType: String,
    @SerializedName("key")
    val key: String
)
