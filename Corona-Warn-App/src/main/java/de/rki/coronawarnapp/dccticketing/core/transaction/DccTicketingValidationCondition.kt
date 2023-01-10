package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingValidationCondition(
    @JsonProperty("hash")
    val hash: String?, // (optional)	Hash of the DCC
    @JsonProperty("lang")
    val lang: String?, // (optional)	Selected language
    @JsonProperty("fnt")
    val fnt: String?, // (optional)	Transliterated family name
    @JsonProperty("gnt")
    val gnt: String?, // (optional)	Transliterated given name
    @JsonProperty("dob")
    val dob: String?, // (optional)	Date of birth
    @JsonProperty("type")
    val type: List<String>?, // (optional)	The acceptable type of DCC
    @JsonProperty("coa")
    val coa: String?, // (optional)	Country of arrival
    @JsonProperty("roa")
    val roa: String?, // (optional)	Region of arrival
    @JsonProperty("cod")
    val cod: String?, // (optional)	Country of departure
    @JsonProperty("rod")
    val rod: String?, // (optional)	Region of departure
    @JsonProperty("category")
    val category: List<String>?, // (optional)	Category for validation
    @JsonProperty("validationClock")
    val validationClock: String?, // (optional)	ISO8601 date where the DCC must be validatable
    @JsonProperty("validFrom")
    val validFrom: String?, // (optional)	ISO8601 date where the DCC must be valid from
    @JsonProperty("validTo")
    val validTo: String?, // (optional)	ISO8601 date where the DCC must be valid to
) : Parcelable
