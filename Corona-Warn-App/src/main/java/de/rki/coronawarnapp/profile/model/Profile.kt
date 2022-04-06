package de.rki.coronawarnapp.profile.model

import org.joda.time.LocalDate

data class Profile(
    val id: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: LocalDate? = null,
    val street: String = "",
    val zipCode: String = "",
    val city: String = "",
    val phone: String = "",
    val email: String = ""
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

