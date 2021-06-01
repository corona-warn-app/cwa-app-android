package de.rki.coronawarnapp.covidcertificate.test

import dagger.Reusable
import okio.ByteString
import javax.inject.Inject

@Reusable
class TestCertificateQRCodeExtractor @Inject constructor() {

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    fun extract(
        decryptionKey: ByteArray,
        encryptedCoseComponents: ByteString,
    ): TestCertificateData {
        throw NotImplementedError()
    }

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    fun extract(qrCode: String): TestCertificateQRCode {
        throw NotImplementedError()
    }
}
