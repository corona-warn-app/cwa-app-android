package de.rki.coronawarnapp.profile.ui.create

import de.rki.coronawarnapp.profile.legacy.RATProfile
import org.joda.time.LocalDate

@Deprecated("Legacy data class")
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
