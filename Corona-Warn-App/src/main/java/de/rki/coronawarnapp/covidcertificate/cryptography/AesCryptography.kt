package de.rki.coronawarnapp.covidcertificate.cryptography

import com.google.android.gms.common.util.Hex
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
        get() = IvParameterSpec(Hex.stringToBytes("00000000000000000000000000000000"))
}

private const val ALGORITHM = "AES"
private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
