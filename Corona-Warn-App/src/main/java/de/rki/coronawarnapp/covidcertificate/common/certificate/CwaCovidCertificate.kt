package de.rki.coronawarnapp.covidcertificate.common.certificate

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.serialization.adapter.RuntimeTypeAdapterFactory
import org.joda.time.Instant

/**
 * For use with the UI
 */
interface CwaCovidCertificate {
    // Header
    val headerIssuer: String
    val headerIssuedAt: Instant
    val headerExpiresAt: Instant

    val qrCodeToDisplay: CoilQrCode
    val firstName: String?
    val lastName: String
    val fullName: String
    val fullNameFormatted: String
    val fullNameStandardizedFormatted: String
    val dateOfBirthFormatted: String
    val personIdentifier: CertificatePersonIdentifier
    val certificateIssuer: String
    val certificateCountry: String
    val certificateId: String

    /**
     * The ID of the container holding this certificate in the CWA.
     */
    val containerId: CertificateContainerId

    val rawCertificate: DccV1.MetaData

    val dccData: DccData<out DccV1.MetaData>

    val notifiedExpiresSoonAt: Instant?
    val notifiedExpiredAt: Instant?

    val lastSeenStateChange: State?
    val lastSeenStateChangeAt: Instant?

    /**
     * The current state of the certificate, see [State]
     */
    fun getState(): State

    val isValid get() = getState() is State.Valid || getState() is State.ExpiringSoon

    /**
     * Requires RuntimeAdapterFactory, see [SerializationModule]
     */
    @Keep
    sealed class State(val type: String) {
        data class Valid(
            @SerializedName("expiresAt") val expiresAt: Instant,
        ) : State("Valid")

        data class ExpiringSoon(
            @SerializedName("expiresAt") val expiresAt: Instant,
        ) : State("ExpiringSoon")

        data class Expired(
            @SerializedName("expiredAt") val expiredAt: Instant,
        ) : State("Expired")

        data class Invalid(
            @SerializedName("isInvalidSignature") val isInvalidSignature: Boolean = true
        ) : State("Invalid") {
            companion object {
                const val URL_INVALID_SIGNATURE_DE = "https://www.coronawarn.app/de/faq/#hc_signature_invalid"
                const val URL_INVALID_SIGNATURE_EN = "https://www.coronawarn.app/en/faq/#hc_signature_invalid"
            }
        }

        companion object {
            const val TYPE_FIELD_NAME = "typeName"
            val typeAdapter = RuntimeTypeAdapterFactory.of(State::class.java, "type", true)
                .registerSubtype(Valid::class.java, "Valid")
                .registerSubtype(ExpiringSoon::class.java, "ExpiringSoon")
                .registerSubtype(Expired::class.java, "Expired")
                .registerSubtype(Invalid::class.java, "Invalid")
        }
    }
}
