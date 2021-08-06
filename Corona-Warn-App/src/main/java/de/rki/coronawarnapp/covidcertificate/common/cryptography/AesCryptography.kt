package de.rki.coronawarnapp.covidcertificate.common.cryptography

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class AesCryptography @Inject constructor() {

    fun decrypt(
        key: ByteArray,
        encryptedData: ByteArray,
        iv: IvParameterSpec? = null
    ): ByteArray = with(Cipher.getInstance(TRANSFORMATION)) {
        val keySpec = SecretKeySpec(key, ALGORITHM)
        val ivParameterSpec = iv ?: defaultIvParameterSpec
        init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)
        doFinal(encryptedData)
    }

    fun encrypt(
        key: ByteArray,
        data: ByteArray,
        iv: IvParameterSpec? = null,
    ): ByteArray = with(Cipher.getInstance(TRANSFORMATION)) {
        val keySpec = SecretKeySpec(key, ALGORITHM)
        val ivParameterSpec = iv ?: defaultIvParameterSpec
        init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)
        doFinal(data)
    }

    private val defaultIvParameterSpec
        get() = IvParameterSpec(
            ByteArray(16) { 0 }
        )
}

private const val ALGORITHM = "AES"
private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
