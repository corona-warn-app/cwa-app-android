package de.rki.coronawarnapp.util.encryption.aes

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class AesCryptography @Inject constructor() {

    fun decryptWithCBC(
        key: ByteArray,
        encryptedData: ByteArray,
        iv: IvParameterSpec = defaultIvParameterSpec
    ): ByteArray = with(Cipher.getInstance(TRANSFORMATION_CBC)) {
        val keySpec = SecretKeySpec(key, ALGORITHM)
        init(Cipher.DECRYPT_MODE, keySpec, iv)
        doFinal(encryptedData)
    }

    fun encryptWithCBC(
        key: ByteArray,
        data: ByteArray,
        iv: IvParameterSpec = defaultIvParameterSpec
    ): ByteArray = with(Cipher.getInstance(TRANSFORMATION_CBC)) {
        val keySpec = SecretKeySpec(key, ALGORITHM)
        init(Cipher.ENCRYPT_MODE, keySpec, iv)
        doFinal(data)
    }

    fun encryptWithGCM(
        key: ByteArray,
        data: ByteArray,
        iv: ByteArray,
    ): ByteArray {
        val gcmParameterSpec = GCMParameterSpec(iv.size * 8, iv)
        val keySpec = SecretKeySpec(key, ALGORITHM)
        with(Cipher.getInstance(TRANSFORMATION_GCM)) {
            init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec)
            return doFinal(data)
        }
    }

    private val defaultIvParameterSpec
        get() = IvParameterSpec(
            ByteArray(16) { 0 }
        )
}

private const val ALGORITHM = "AES"
private const val TRANSFORMATION_CBC = "AES/CBC/PKCS5Padding"
private const val TRANSFORMATION_GCM = "AES/GCM/NoPadding"
