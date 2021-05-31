package de.rki.coronawarnapp.coronatest.type.pcr

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestCertificate
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import okio.ByteString
import org.joda.time.Instant

data class PCRTestCertificate(
    @SerializedName("identifier")
    override val identifier: String,
    @SerializedName("registrationToken")
    override val registrationToken: RegistrationToken,

    @SerializedName("registeredAt")
    override val registeredAt: Instant,

    @SerializedName("rsaPublicKey")
    override val rsaPublicKey: RSAKey.Public,

    @SerializedName("rsaPrivateKey")
    override val rsaPrivateKey: RSAKey.Private,

    @SerializedName("isPublicKeyRegistered")
    override val isPublicKeyRegistered: Boolean = false,

    @SerializedName("encryptedDataEncryptionkey")
    override val encryptedDataEncryptionkey: ByteString? = null,

    @SerializedName("encryptedDgcCose")
    override val encryptedDgcCose: ByteString? = null,

    @SerializedName("testCertificateQrCode")
    override val testCertificateQrCode: String? = null,
) : TestCertificate {

    override val type: CoronaTest.Type
        get() = CoronaTest.Type.PCR
}
