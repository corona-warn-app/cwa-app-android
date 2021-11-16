package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DccTicketingResultToken(
    @SerializedName("iss")
    val iss: String, // The URL of the issuer
    @SerializedName("iat")
    val iat: Int, // The issued at timestamp in seconds
    @SerializedName("exp")
    val exp: Int, // The expiration timestamp in seconds
    @SerializedName("sub")
    val sub: String, // The subject of the transaction
    @SerializedName("category")
    val category: List<String>, // The categories of confirmation
    @SerializedName("result")
    val result: String, // The result of the validation (OK for pass, CHK for open, NOK for fail)
    @SerializedName("results")
    val results: List<DccTicketingResultItem>, // An array of Result Item objects
    @SerializedName("confirmation")
    val confirmation: String, // A JWT token with reduced set of information about the result.
) : Parcelable
