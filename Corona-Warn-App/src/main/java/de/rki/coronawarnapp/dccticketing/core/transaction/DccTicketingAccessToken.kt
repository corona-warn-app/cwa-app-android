package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingAccessToken(
    @SerializedName("iss")
    val iss: String, // The URL of the service provider
    @SerializedName("iat")
    val iat: Int, // The issued at timestamp in seconds
    @SerializedName("exp")
    val exp: Int, // The expiration timestamp in seconds
    @SerializedName("sub")
    val sub: String, // The subject of the transaction
    @SerializedName("aud")
    val aud: String, // The URL of the validation service
    @SerializedName("jti")
    val jti: String, // The token identifier
    @SerializedName("v")
    val v: String, // A version information
    @SerializedName("t")
    val t: Int, // The type of the validation(0 = Structure, 1 = Cryptographic, 2 = Full)
    @SerializedName("vc")
    val vc: DccTicketingValidationCondition?, // A data structure representing the validation conditions
) : Parcelable
