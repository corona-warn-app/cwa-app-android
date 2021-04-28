package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import org.joda.time.LocalDate

data class RATProfileData(
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

fun RATProfileData.toRATProfile() = RATProfile(
    firstName = firstName,
    lastName = lastName,
    birthDate = birthDate,
    street = street,
    zipCode = zipCode,
    city = city,
    phone = phone,
    email = email
)
