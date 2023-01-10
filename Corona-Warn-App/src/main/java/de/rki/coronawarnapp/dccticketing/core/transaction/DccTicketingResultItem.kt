package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingResultItem(
    @JsonProperty("identifier")
    val identifier: String, // Identifier of the check
    @JsonProperty("result")
    val result: DccTicketingResultToken.DccResult, // The result of the validation (OK = pass, CHK = open, NOK = fail)
    @JsonProperty("type")
    val type: String, // the type of check
    @JsonProperty("details")
    val details: String, // Description of the check
) : Parcelable
