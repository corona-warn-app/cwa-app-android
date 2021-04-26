package de.rki.coronawarnapp.coronatest.antigen.profile

import org.joda.time.LocalDate

data class RATProfile(
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: LocalDate? = null,
    val street: String = "",
    val zipCode: String = "",
    val city: String = "",
    val phone: String = "",
    val email: String = "",
) {
    val isValid: Boolean = firstName.isNotBlank() ||
        lastName.isNotBlank() ||
        birthDate != null ||
        street.isNotBlank() ||
        zipCode.isNotBlank() ||
        city.isNotBlank() ||
        phone.isNotBlank() ||
        email.isNotBlank()
}

/*
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("birthDate") val birthDate: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("zipCode") val zipCode: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
 */
