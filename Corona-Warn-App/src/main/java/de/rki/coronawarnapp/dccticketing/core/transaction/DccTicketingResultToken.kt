package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DccTicketingResultToken(
    val iss: String,            //The URL of the issuer
    val iat: Int,                //The issued at timestamp in seconds
    val exp: Int,                //The expiration timestamp in seconds
    val sub: String,            //The subject of the transaction
    val category: List<String>,    //The categories of confirmation
    val result: String,            //The result of the validation (OK for pass, CHK for open, NOK for fail)
    val results: List<DccTicketingResultItem>,        //An array of Result Item objects (see below)
    val confirmation: String,    //A JWT token with reduced set of information about the result.
) : Parcelable
