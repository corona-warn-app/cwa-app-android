package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK.Purpose
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccJWK(
    /**
     * an array of base64-encoded strings that each represent an x509 certificate.
     *
     * Each element typically needs to be parsed to a native x509 certificate object to obtain certificate information
     * such as the included public key.
     *
     * It is safe to assume that there is at least one entry
     */
    @JsonProperty("x5c") val x5c: List<String>,

    /**
     * a base64-encoded string.
     *
     * The attribute can typically be treated as a regular string and there is no need to parse
     * this to a byte sequence ( Data / ByteArray ).
     */
    @JsonProperty("kid") val kid: String,

    /**
     * a string describing the algorithm.
     *
     * The attribute typically has one of the values ES256 , RS256 , or PS256. However, the data structure should allow
     * for other values.
     */
    @JsonProperty("alg") val alg: String,

    /** a value of either [Purpose.SIGNATURE] or [Purpose.ENCRYPTION] that indicates the purpose of the JWK (signature or encryption) */
    @JsonProperty("use") val use: Purpose,
) : Parcelable {

    enum class Purpose {
        @JsonProperty("sig")
        SIGNATURE,

        @JsonProperty("enc")
        ENCRYPTION
    }
}
