package de.rki.coronawarnapp.covidcertificate.test.core.storage

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import okio.ByteString
import org.joda.time.Instant

interface StoredTestCertificateData {
    val identifier: TestCertificateIdentifier
    val registrationToken: RegistrationToken
    val type: CoronaTest.Type
    val registeredAt: Instant
    val publicKeyRegisteredAt: Instant?
    val rsaPublicKey: RSAKey.Public?
    val rsaPrivateKey: RSAKey.Private?
    val certificateReceivedAt: Instant?
    val encryptedDataEncryptionkey: ByteString?
    val encryptedDccCose: ByteString?
    val testCertificateQrCode: String?
    val labId: String?
    val certificateSeenByUser: Boolean
}
