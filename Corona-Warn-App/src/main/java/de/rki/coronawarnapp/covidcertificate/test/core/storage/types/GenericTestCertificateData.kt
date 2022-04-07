package de.rki.coronawarnapp.covidcertificate.test.core.storage.types

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import org.joda.time.Instant

/**
 * A generic data class used to store data for scanned qrcodes.
 * May be cloned if we need to support different types of test certificates in the future.
 */
data class GenericTestCertificateData(
    @SerializedName("identifier")
    override val identifier: String,

    @SerializedName("registeredAt")
    override val registeredAt: Instant,

    @SerializedName("certificateReceivedAt")
    override val certificateReceivedAt: Instant? = null,

    @SerializedName("notifiedInvalidAt")
    override val notifiedInvalidAt: Instant? = null,

    @SerializedName("lastSeenStateChange")
    override val lastSeenStateChange: State? = null,

    @SerializedName("notifiedBlockedAt")
    override val notifiedBlockedAt: Instant? = null,

    @SerializedName("notifiedRevokedAt")
    override val notifiedRevokedAt: Instant? = null,

    @SerializedName("lastSeenStateChangeAt")
    override val lastSeenStateChangeAt: Instant? = null,

    @SerializedName("testCertificateQrCode")
    override val testCertificateQrCode: String? = null,

    // Imported Certificates are already scanned some time ago and shouldn't show a badge
    @SerializedName("certificateSeenByUser")
    override val certificateSeenByUser: Boolean = true,

    @SerializedName("recycledAt")
    override val recycledAt: Instant? = null,
) : ScannedTestCertificate() {

    // Otherwise GSON unsafes reflection to create this class, and sets the LAZY to null
    @Suppress("unused")
    constructor() : this(
        identifier = "",
        registeredAt = Instant.EPOCH
    )
}
