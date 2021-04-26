package de.rki.coronawarnapp.coronatest.antigen.profile

import com.google.gson.annotations.SerializedName

data class RATProfile(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("birthDate") val birthDate: String,
    @SerializedName("street") val street: String,
    @SerializedName("zipCode") val zipCode: String,
    @SerializedName("city") val city: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
)
