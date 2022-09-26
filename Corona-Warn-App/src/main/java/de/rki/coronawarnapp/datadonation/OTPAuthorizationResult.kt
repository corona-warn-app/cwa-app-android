package de.rki.coronawarnapp.datadonation

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

@Keep
data class OTPAuthorizationResult(
    @JsonProperty("uuid")
    val uuid: UUID,
    @JsonProperty("authorized")
    val authorized: Boolean,
    @JsonProperty("redeemedAt")
    val redeemedAt: Instant,
    @JsonProperty("invalidated")
    val invalidated: Boolean = false
) {

    fun toInvalidatedInstance() = copy(invalidated = true)
}
