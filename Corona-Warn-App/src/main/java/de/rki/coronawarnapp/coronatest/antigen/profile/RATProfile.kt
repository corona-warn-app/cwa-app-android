package de.rki.coronawarnapp.coronatest.antigen.profile

import com.google.gson.annotations.SerializedName

data class RATProfile(
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("birthDate") val birthDate: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("zipCode") val zipCode: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
) {
    val isValid: Boolean = !firstName.isNullOrBlank() ||
        !lastName.isNullOrBlank() ||
        !birthDate.isNullOrBlank() ||
        !street.isNullOrBlank() ||
        !zipCode.isNullOrBlank() ||
        !city.isNullOrBlank() ||
        !phone.isNullOrBlank() ||
        !email.isNullOrBlank()
}
