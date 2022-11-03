package de.rki.coronawarnapp.srs.core.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

data class SrsOtp(
    @JsonProperty("otp")
    val uuid: UUID = UUID.randomUUID(),
    @JsonProperty("expiresAt")
    val expiresAt: Instant = Instant.MIN
) {

    fun isValid(now: Instant = Instant.now()) = expiresAt >= now
}
