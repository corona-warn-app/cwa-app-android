package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class DccTicketingValidationCondition(
    val hash: String?,              //(optional)	Hash of the DCC
    val lang: String?,              //(optional)	Selected language
    val fnt: String?,               //(optional)	Transliterated family name
    val gnt: String?,               //(optional)	Transliterated given name
    val dob: String?,               //(optional)	Date of birth
    val type: List<String>?,        //(optional)	The acceptable type of DCC
    val coa: String?,               //(optional)	Country of arrival
    val roa: String?,               //(optional)	Region of arrival
    val cod: String?,               //(optional)	Country of departure
    val rod: String?,               //(optional)	Region of departure
    val category: List<String>?,    //(optional)	Category for validation
    val validationClock: String?,   //(optional)	ISO8601 date where the DCC must be validatable
    val validFrom: String?,         //(optional)	ISO8601 date where the DCC must be valid from
    val validTo: String?,           //(optional)	ISO8601 date where the DCC must be valid to
) : Parcelable
