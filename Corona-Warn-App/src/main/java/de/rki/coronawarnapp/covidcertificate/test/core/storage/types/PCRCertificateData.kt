package de.rki.coronawarnapp.covidcertificate.test.core.storage.types

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import okio.ByteString
import java.time.Instant

data class PCRCertificateData internal constructor(
    @JsonProperty("identifier")
    override val identifier: String,

    @JsonProperty("registrationToken")
    override val registrationToken: RegistrationToken,

    @JsonProperty("registeredAt")
    override val registeredAt: Instant,

    @JsonProperty("notifiedInvalidAt")
    override val notifiedInvalidAt: Instant? = null,

    @JsonProperty("notifiedBlockedAt")
    override val notifiedBlockedAt: Instant? = null,

    @JsonProperty("notifiedRevokedAt")
    override val notifiedRevokedAt: Instant? = null,

    @JsonProperty("lastSeenStateChange")
    override val lastSeenStateChange: State? = null,

    @JsonProperty("lastSeenStateChangeAt")
    override val lastSeenStateChangeAt: Instant? = null,

    @JsonProperty("publicKeyRegisteredAt")
    override val publicKeyRegisteredAt: Instant? = null,

    @JsonProperty("rsaPublicKey")
    override val rsaPublicKey: RSAKey.Public? = null,

    @JsonProperty("rsaPrivateKey")
    override val rsaPrivateKey: RSAKey.Private? = null,

    @JsonProperty("certificateReceivedAt")
    override val certificateReceivedAt: Instant? = null,

    @JsonProperty("encryptedDataEncryptionkey")
    override val encryptedDataEncryptionkey: ByteString? = null,

    @JsonProperty("encryptedDccCose")
    override val encryptedDccCose: ByteString? = null,

    @JsonProperty("testCertificateQrCode")
    override val testCertificateQrCode: String? = null,

    @JsonProperty("labId")
    override val labId: String? = null,

    @JsonProperty("certificateSeenByUser")
    override val certificateSeenByUser: Boolean = false,

    @JsonProperty("recycledAt")
    override val recycledAt: Instant? = null,
) : RetrievedTestCertificate() {

    // Otherwise GSON unsafes reflection to create this class, and sets the LAZY to null
    @Suppress("unused")
    constructor() : this(
        identifier = "",
        registrationToken = "",
        registeredAt = Instant.EPOCH
    )
}
