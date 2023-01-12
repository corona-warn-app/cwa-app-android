package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingResultToken(
    @JsonProperty("iss")
    val iss: String, // The URL of the issuer
    @JsonProperty("iat")
    val iat: Long, // The issued at timestamp in seconds
    @JsonProperty("exp")
    val exp: Long, // The expiration timestamp in seconds
    @JsonProperty("sub")
    val sub: String, // The subject of the transaction
    @JsonProperty("category")
    val category: List<String>, // The categories of confirmation
    @JsonProperty("result")
    val result: DccResult, // The result of the validation (OK for pass, CHK for open, NOK for fail)
    @JsonProperty("results")
    val results: List<DccTicketingResultItem>, // An array of Result Item objects
    @JsonProperty("confirmation")
    val confirmation: String, // A JWT token with reduced set of information about the result.
) : Parcelable {

    enum class DccResult {
        @JsonProperty("OK")
        PASS,

        @JsonProperty("CHK")
        OPEN,

        @JsonProperty("NOK")
        FAIL
    }
}
