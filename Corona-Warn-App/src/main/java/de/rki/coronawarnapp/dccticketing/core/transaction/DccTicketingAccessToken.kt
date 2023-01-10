package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingAccessToken(
    @JsonProperty("iss")
    val iss: String, // The URL of the service provider
    @JsonProperty("iat")
    val iat: Int, // The issued at timestamp in seconds
    @JsonProperty("exp")
    val exp: Int, // The expiration timestamp in seconds
    @JsonProperty("sub")
    val sub: String, // The subject of the transaction
    @JsonProperty("aud")
    val aud: String, // The URL of the validation service
    @JsonProperty("jti")
    val jti: String, // The token identifier
    @JsonProperty("v")
    val v: String, // A version information
    @JsonProperty("t")
    val t: Int, // The type of the validation(0 = Structure, 1 = Cryptographic, 2 = Full)
    @JsonProperty("vc")
    val vc: DccTicketingValidationCondition?, // A data structure representing the validation conditions
) : Parcelable
