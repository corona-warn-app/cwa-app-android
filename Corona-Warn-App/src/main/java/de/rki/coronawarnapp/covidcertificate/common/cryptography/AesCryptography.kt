package de.rki.coronawarnapp.covidcertificate.common.cryptography

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class AesCryptography @Inject constructor() {

    fun decrypt(
        decryptionKey: ByteArray,
        encryptedData: ByteArray
    ): ByteArray = with(Cipher.getInstance(TRANSFORMATION)) {
        val keySpec = SecretKeySpec(decryptionKey, ALGORITHM)
        init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)
        doFinal(encryptedData)
    }

    private val ivParameterSpec
        get() = IvParameterSpec(
            ByteArray(16) { 0 }
        )
}

private const val ALGORITHM = "AES"
private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
