package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DccTicketingAccessToken(
    val iss: String,                            //The URL of the service provider
    val iat: Int,                               //The issued at timestamp in seconds
    val exp: Int,                               //The expiration timestamp in seconds
    val sub: String,                            //The subject of the transaction
    val aud: String,                            //The URL of the validation service
    val jti: String,                            //The token identifier
    val v: String,                              //A version information
    val t: Int,                                 //The type of the validation (0 = Structure, 1 = Cryptographic, 2 = Full)
    val vc: DccTicketingValidationCondition?,   //A data structure representing the validation conditions (see below)
) : Parcelable
