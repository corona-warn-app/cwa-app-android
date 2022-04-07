package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.google.gson.annotations.SerializedName

data class RevocationEntryCoordinates(
    @SerializedName("kid")
    val kid: String,

    @SerializedName("type")
    val type: Type,

    @SerializedName("x")
    val x: String,

    @SerializedName("y")
    val y: String
) {

    enum class Type(type: String) {
        SIGNATURE("0a"),
        UCI("0b"),
        COUNTRYCODEUCI("0c")
    }
}
