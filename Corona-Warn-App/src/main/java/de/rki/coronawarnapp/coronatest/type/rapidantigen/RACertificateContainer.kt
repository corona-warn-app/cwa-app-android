package de.rki.coronawarnapp.coronatest.type.rapidantigen

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestCertificateContainer
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import okio.ByteString
import org.joda.time.Instant

data class RACertificateContainer(
    @SerializedName("identifier")
    override val identifier: String,

    @SerializedName("registrationToken")
    override val registrationToken: RegistrationToken,

    @SerializedName("registeredAt")
    override val registeredAt: Instant,

    @SerializedName("publicKeyRegisteredAt")
    override val publicKeyRegisteredAt: Instant? = null,

    @SerializedName("rsaPublicKey")
    override val rsaPublicKey: RSAKey.Public? = null,

    @SerializedName("rsaPrivateKey")
    override val rsaPrivateKey: RSAKey.Private? = null,

    @SerializedName("certificateReceivedAt")
    override val certificateReceivedAt: Instant? = null,

    @SerializedName("encryptedDataEncryptionkey")
    override val encryptedDataEncryptionkey: ByteString? = null,

    @SerializedName("encryptedDccCose")
    override val encryptedDccCose: ByteString? = null,

    @SerializedName("testCertificateQrCode")
    override val testCertificateQrCode: String? = null,

    @Transient override val isUpdatingData: Boolean = false,
) : TestCertificateContainer() {

    // Otherwise GSON unsafes reflection to create this class, and sets the LAZY to null
    @Suppress("unused")
    constructor() : this(
        identifier = "",
        registrationToken = "",
        registeredAt = Instant.EPOCH
    )

    override val type: CoronaTest.Type
        get() = CoronaTest.Type.RAPID_ANTIGEN
}
