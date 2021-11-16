package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccJWK(
    val x5c: List<String>, // base64-encoded list of strings that represents an x509 certificate
    val kid: String, // base64-encoded string
    val alg: String, // string describing the algorithm
    val use: Purpose,
) : Parcelable {

    enum class Purpose {
        @SerializedName("sig")
        SIGNATURE,

        @SerializedName("enc")
        ENCRYPTION
    }
}
