package de.rki.coronawarnapp.http.requests

import com.google.gson.annotations.SerializedName

data class ReqistrationRequest(
    @SerializedName("registrationToken")
    val registrationToken: String
)
