package de.rki.coronawarnapp.covidcertificate.cryptography

import com.google.android.gms.common.util.Hex
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class AesCryptography @Inject constructor() {

    private val ivParameterSpec
        get() = IvParameterSpec(Hex.stringToBytes("00000000000000000000000000000000"))

    fun decrypt(
        decryptionKey: ByteArray,
        encryptedData: ByteArray
    ): ByteArray {
        val keySpec = SecretKeySpec((decryptionKey), ALGORITHM)
        val input = encryptedData
        return with(Cipher.getInstance(TRANSFORMATION)) {
            init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)
            doFinal(input)
        }
    }
}

private const val ALGORITHM = "AES"
private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
