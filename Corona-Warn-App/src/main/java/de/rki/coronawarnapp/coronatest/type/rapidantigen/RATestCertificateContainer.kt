package de.rki.coronawarnapp.coronatest.type.rapidantigen

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateQRCodeExtractor
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import okio.ByteString
import org.joda.time.Instant

data class RATestCertificateContainer(
    @SerializedName("identifier")
    override val identifier: String,

    @SerializedName("registrationToken")
    override val registrationToken: RegistrationToken,

    @SerializedName("registeredAt")
    override val registeredAt: Instant,

    @SerializedName("isPublicKeyRegistered")
    override val isPublicKeyRegistered: Boolean = false,

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
) : TestCertificate {

    // Otherwise GSON unsafes reflection to create this class, and sets the LAZY to null
    @Suppress("unused")
    constructor() : this(
        identifier = "",
        registrationToken = "",
        registeredAt = Instant.EPOCH
    )

    // Either set by [ContainerPostProcessor] or during first update
    @Transient override lateinit var qrCodeExtractor: TestCertificateQRCodeExtractor
    @Transient override var preParsedData: TestCertificateData? = null

    @delegate:Transient
    override val certificateData: TestCertificateData? by lazy {
        preParsedData ?: testCertificateQrCode?.let { qrCodeExtractor.extract(it).testCertificateData }
    }

    override val type: CoronaTest.Type
        get() = CoronaTest.Type.RAPID_ANTIGEN
}
