package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import okio.ByteString
import org.joda.time.Instant

interface TestCertificate {
    val identifier: TestCertificateIdentifier
    val registrationToken: RegistrationToken
    val type: CoronaTest.Type
    val registeredAt: Instant
    val rsaPublicKey: RSAKey.Public
    val rsaPrivateKey: RSAKey.Private
    val isPublicKeyRegistered: Boolean
    val encryptedDataEncryptionkey: ByteString?
    val encryptedDccCose: ByteString?
    val testCertificateQrCode: String?
}

typealias TestCertificateIdentifier = String
