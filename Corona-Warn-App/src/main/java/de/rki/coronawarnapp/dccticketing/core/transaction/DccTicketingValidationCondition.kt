package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingValidationCondition(
    @SerializedName("hash")
    val hash: String?, // (optional)	Hash of the DCC
    @SerializedName("lang")
    val lang: String?, // (optional)	Selected language
    @SerializedName("fnt")
    val fnt: String?, // (optional)	Transliterated family name
    @SerializedName("gnt")
    val gnt: String?, // (optional)	Transliterated given name
    @SerializedName("dob")
    val dob: String?, // (optional)	Date of birth
    @SerializedName("type")
    val type: List<String>?, // (optional)	The acceptable type of DCC
    @SerializedName("coa")
    val coa: String?, // (optional)	Country of arrival
    @SerializedName("roa")
    val roa: String?, // (optional)	Region of arrival
    @SerializedName("cod")
    val cod: String?, // (optional)	Country of departure
    @SerializedName("rod")
    val rod: String?, // (optional)	Region of departure
    @SerializedName("category")
    val category: List<String>?, // (optional)	Category for validation
    @SerializedName("validationClock")
    val validationClock: String?, // (optional)	ISO8601 date where the DCC must be validatable
    @SerializedName("validFrom")
    val validFrom: String?, // (optional)	ISO8601 date where the DCC must be valid from
    @SerializedName("validTo")
    val validTo: String?, // (optional)	ISO8601 date where the DCC must be valid to
) : Parcelable
