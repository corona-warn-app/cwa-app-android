package de.rki.coronawarnapp.datadonation

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.joda.time.Instant
import java.util.UUID

@Keep
data class OTPAuthorizationResult(
    @SerializedName("uuid")
    val uuid: UUID,
    @SerializedName("expirationDate")
    val expirationDate: Instant,
    @SerializedName("authorized")
    val authorized: Boolean,
    @SerializedName("redeemedAt")
    val redeemedAt: Instant
)
