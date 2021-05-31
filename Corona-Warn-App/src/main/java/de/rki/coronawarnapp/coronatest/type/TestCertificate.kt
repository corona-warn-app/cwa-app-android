package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.covidcertificate.test.TestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateDccV1
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateQRCodeExtractor
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import de.rki.coronawarnapp.vaccination.core.certificate.CoseCertificateHeader
import okio.ByteString
import org.joda.time.Instant

interface TestCertificate {
    val identifier: TestCertificateIdentifier
    val registrationToken: RegistrationToken
    val type: CoronaTest.Type
    val registeredAt: Instant
    val isPublicKeyRegistered: Boolean
    val rsaPublicKey: RSAKey.Public?
    val rsaPrivateKey: RSAKey.Private?
    val certificateReceivedAt: Instant?
    val encryptedDataEncryptionkey: ByteString?
    val encryptedDccCose: ByteString?
    val testCertificateQrCode: String?

    val isCertRetrieved: Boolean
        get() = certificateReceivedAt != null

    var qrCodeExtractor: TestCertificateQRCodeExtractor

    var preParsedData: TestCertificateData?
    val certificateData: TestCertificateData?

    val header: CoseCertificateHeader?
        get() = certificateData?.header

    val certificate: TestCertificateDccV1?
        get() = certificateData?.certificate

    val testCertificate: TestCertificateDccV1.TestCertificateData?
        get() = certificate?.testCertificateData?.single()

    val certificateId: String?
        get() = testCertificate?.uniqueCertificateIdentifier
}

typealias TestCertificateIdentifier = String
