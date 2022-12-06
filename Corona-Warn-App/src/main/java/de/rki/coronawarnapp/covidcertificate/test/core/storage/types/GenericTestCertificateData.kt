package de.rki.coronawarnapp.covidcertificate.test.core.storage.types

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import java.time.Instant

/**
 * A generic data class used to store data for scanned qrcodes.
 * May be cloned if we need to support different types of test certificates in the future.
 */
data class GenericTestCertificateData(
    @JsonProperty("identifier")
    override val identifier: String,

    @JsonProperty("registeredAt")
    override val registeredAt: Instant,

    @JsonProperty("certificateReceivedAt")
    override val certificateReceivedAt: Instant? = null,

    @JsonProperty("notifiedInvalidAt")
    override val notifiedInvalidAt: Instant? = null,

    @JsonProperty("lastSeenStateChange")
    override val lastSeenStateChange: State? = null,

    @JsonProperty("notifiedBlockedAt")
    override val notifiedBlockedAt: Instant? = null,

    @JsonProperty("notifiedRevokedAt")
    override val notifiedRevokedAt: Instant? = null,

    @JsonProperty("lastSeenStateChangeAt")
    override val lastSeenStateChangeAt: Instant? = null,

    @JsonProperty("testCertificateQrCode")
    override val testCertificateQrCode: String? = null,

    // Imported Certificates are already scanned some time ago and shouldn't show a badge
    @JsonProperty("certificateSeenByUser")
    override val certificateSeenByUser: Boolean = true,

    @JsonProperty("recycledAt")
    override val recycledAt: Instant? = null,
) : ScannedTestCertificate() {

    // Otherwise GSON unsafes reflection to create this class, and sets the LAZY to null
    @Suppress("unused")
    constructor() : this(
        identifier = "",
        registeredAt = Instant.EPOCH
    )
}
