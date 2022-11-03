package de.rki.coronawarnapp.srs.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.time.OffsetDateTime

data class SrsAuthorizationResponse(
    @JsonProperty("expirationDate") val expirationDate: String
) {
    @get:JsonIgnore
    val expiresAt: Instant
        get() = OffsetDateTime.parse(expirationDate).toInstant()
}
