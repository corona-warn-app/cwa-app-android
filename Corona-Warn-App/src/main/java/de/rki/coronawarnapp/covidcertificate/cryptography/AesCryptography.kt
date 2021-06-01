package de.rki.coronawarnapp.covidcertificate.cryptography

import com.google.android.gms.common.util.Hex
import dagger.Reusable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Base64.decode
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

@Reusable
class AesCryptography @Inject constructor() {

    private val ivParameterSpec
        get() = IvParameterSpec(Hex.stringToBytes("00000000000000000000000000000000"))

//    fun encrypt(rawData: ByteArray, dek: ByteArray): ByteArray {
//        require(dek.size == 32)
//
//        Security.addProvider(BouncyCastleProvider())
//        val keySpec = SecretKeySpec(dek, ALGORITHM)
//
//        val cipher = Cipher.getInstance(TRANSFORMATION)
//        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)
//
//        val cipherText = ByteArray(cipher.getOutputSize(rawData.size))
//        var ctLength = cipher.update(
//            rawData, 0, rawData.size,
//            cipherText, 0
//        )
//        ctLength += cipher.doFinal(cipherText, ctLength)
//        return cipherText
//    }

    fun decrypt(
        decryptionKey: ByteArray,
        encryptedData: ByteArray
    ): ByteArray {
        Security.addProvider(BouncyCastleProvider())
        val keySpec = SecretKeySpec(decode(decryptionKey), ALGORITHM)
        val input = decode(encryptedData)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)

        val output = ByteArray(cipher.getOutputSize(input.size))
        var ptLength = cipher.update(input, 0, input.size, output, 0)
        ptLength += cipher.doFinal(output, ptLength)
        return output.takeWhile { it.toInt() != 0 }.toByteArray()
    }
}

private const val ALGORITHM = "AES"
private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
