package de.rki.coronawarnapp.covidcertificate

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class AesEncryptor @Inject constructor() {

    private val ivParameterSpec
        get() = IvParameterSpec("0000000000000000".toByteArray(charset("UTF8")))

    fun encrypt(rawData: ByteArray, dek: ByteArray): ByteArray {
        Security.addProvider(BouncyCastleProvider())
        val sKey = SecretKeySpec(dek, ALGORITHM)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, sKey, ivParameterSpec)

        val cipherText = ByteArray(cipher.getOutputSize(rawData.size))
        var ctLength = cipher.update(
            rawData, 0, rawData.size,
            cipherText, 0
        )
        ctLength += cipher.doFinal(cipherText, ctLength)
        return cipherText
    }

    fun decrypt(encryptedData: ByteArray, dek: ByteArray): ByteArray {
        Security.addProvider(BouncyCastleProvider())
        val skey = SecretKeySpec(dek, ALGORITHM)
        val input = org.bouncycastle.util.encoders.Base64
            .decode(encryptedData)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, skey, ivParameterSpec)

        val plainText = ByteArray(cipher.getOutputSize(input.size))
        var ptLength = cipher.update(input, 0, input.size, plainText, 0)
        ptLength += cipher.doFinal(plainText, ptLength)
        return plainText
        //val decryptedString = String(plainText)
        //return decryptedString.trim { it <= ' ' }
    }
}

private const val ALGORITHM = "AES"
private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
