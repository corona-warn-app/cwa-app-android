package de.rki.coronawarnapp.covidcertificate.test.core.storage.types

import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import okio.ByteString
import org.joda.time.Instant

sealed class RetrievedTestCertificate : BaseTestCertificateData() {

    abstract val registrationToken: RegistrationToken

    abstract val publicKeyRegisteredAt: Instant?
    abstract val rsaPublicKey: RSAKey.Public?
    abstract val rsaPrivateKey: RSAKey.Private?

    abstract val encryptedDataEncryptionkey: ByteString?
    abstract val encryptedDccCose: ByteString?

    abstract val labId: String?
    abstract val certificateSeenByUser: Boolean

    val isPublicKeyRegistered: Boolean
        get() = publicKeyRegisteredAt != null
}
