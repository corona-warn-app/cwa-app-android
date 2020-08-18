package de.rki.coronawarnapp.http.requests

import com.google.gson.annotations.SerializedName

data class TanRequestBody(
    @SerializedName("registrationToken")
    val registrationToken: String? = null,
    @SerializedName("requestPadding")
    val requestPadding: String? = null
)
