package de.rki.coronawarnapp.http.responses

import com.google.gson.annotations.SerializedName

data class TanResponse(
    @SerializedName("tan")
    val tan: String
)
