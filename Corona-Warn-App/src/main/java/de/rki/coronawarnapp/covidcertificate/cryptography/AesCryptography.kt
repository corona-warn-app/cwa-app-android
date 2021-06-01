package de.rki.coronawarnapp.covidcertificate.cryptography

import dagger.Reusable
import okio.ByteString
import javax.inject.Inject

@Reusable
class AesCryptography @Inject constructor() {

    fun decrypt(
        decryptionKey: ByteArray,
        encryptedData: ByteString
    ): ByteArray {
        throw NotImplementedError()
    }
}
