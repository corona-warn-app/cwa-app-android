package de.rki.coronawarnapp.coronatest.antigen.profile

import com.google.gson.annotations.SerializedName

data class RATProfile(
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("birthDate")
    val birthDate: String? = null,
) {
    val isValid: Boolean = !firstName.isNullOrBlank() ||
        !lastName.isNullOrBlank() ||
        !birthDate.isNullOrBlank()
}
