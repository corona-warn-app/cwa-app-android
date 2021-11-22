package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK.Purpose
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import okio.ByteString.Companion.decodeBase64
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

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
    @SerializedName("x5c") val x5c: List<String>,

    /**
     * a base64-encoded string.
     *
     * The attribute can typically be treated as a regular string and there is no need to parse
     * this to a byte sequence ( Data / ByteArray ).
     */
    @SerializedName("kid") val kid: String,

    /**
     * a string describing the algorithm.
     *
     * The attribute typically has one of the values ES256 , RS256 , or PS256. However, the data structure should allow
     * for other values.
     */
    @SerializedName("alg") val alg: String,

    /** a value of either [Purpose.SIGNATURE] or [Purpose.ENCRYPTION] that indicates the purpose of the JWK (signature or encryption) */
    @SerializedName("use") val use: Purpose,
) : Parcelable {

    enum class Purpose {
        @SerializedName("sig")
        SIGNATURE,

        @SerializedName("enc")
        ENCRYPTION
    }

    @IgnoredOnParcel
    private val certificateFactory by lazy {
        CertificateFactory.getInstance("X.509")
    }

    val publicKey: PublicKey?
        get() = x5c.first()
            .decodeBase64()
            ?.toByteArray()
            ?.inputStream()
            .use {
                certificateFactory.generateCertificate(it) as X509Certificate
            }.publicKey
}
