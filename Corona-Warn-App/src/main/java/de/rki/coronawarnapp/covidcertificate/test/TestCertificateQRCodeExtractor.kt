package de.rki.coronawarnapp.covidcertificate.test

import dagger.Reusable
import javax.inject.Inject

@Reusable
class TestCertificateQRCodeExtractor @Inject constructor() {

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    fun extract(
        decryptionKey: ByteArray,
        encryptedCoseComponents: ByteArray,
    ): TestCertificateQRCode {
        throw NotImplementedError()
    }

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    fun extract(qrCode: String): TestCertificateQRCode {
        throw NotImplementedError()
    }
}
