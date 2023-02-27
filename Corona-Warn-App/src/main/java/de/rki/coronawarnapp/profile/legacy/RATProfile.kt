package de.rki.coronawarnapp.profile.legacy

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

// Deprecated("Legacy data class")
data class RATProfile(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("birthDate") val birthDate: LocalDate?,
    @JsonProperty("street") val street: String,
    @JsonProperty("zipCode") val zipCode: String,
    @JsonProperty("city") val city: String,
    @JsonProperty("phone") val phone: String,
    @JsonProperty("email") val email: String,
)
