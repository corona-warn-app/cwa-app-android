package de.rki.coronawarnapp.datadonation

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.util.UUID

@Keep
data class OTPAuthorizationResult(
    @SerializedName("uuid")
    val uuid: UUID,
    @SerializedName("authorized")
    val authorized: Boolean,
    @SerializedName("redeemedAt")
    val redeemedAt: Instant,
    @SerializedName("invalidated")
    val invalidated: Boolean = false
) {

    fun toInvalidatedInstance() = copy(invalidated = true)
}
