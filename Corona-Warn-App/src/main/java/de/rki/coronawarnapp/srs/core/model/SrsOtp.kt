package de.rki.coronawarnapp.srs.core.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class SrsOtp(
    @JsonProperty("otp")
    val otp: String,
    @JsonProperty("expiresAt")
    val expiresAt: Instant
)
