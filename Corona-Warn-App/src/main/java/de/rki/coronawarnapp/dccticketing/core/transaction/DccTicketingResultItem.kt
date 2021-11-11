package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DccTicketingResultItem(
    @SerializedName("identifier")
    val identifier: String,    //Identifier of the check
    @SerializedName("result")
    val result: String,        //The result of the validation (OK for pass, CHK for open, NOK for fail)
    @SerializedName("type")
    val type: String,        //he type of check
    @SerializedName("details")
    val details: String,     //Description of the check
) : Parcelable
