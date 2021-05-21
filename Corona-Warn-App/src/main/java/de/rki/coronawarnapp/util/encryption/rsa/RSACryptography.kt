package de.rki.coronawarnapp.util.encryption.rsa

import dagger.Reusable
import okio.ByteString
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.inject.Inject

@Reusable
class RSACryptography @Inject constructor() {

    fun decrypt(
        toDecrypt: ByteString,
        privateKey: RSAKey.Private,
        cipherType: CipherType = CipherType.RSA_PKCS1_OAEP_PADDING,
    ): ByteString {

        val cipher = Cipher.getInstance(cipherType.transformation).apply {
            init(Cipher.DECRYPT_MODE, privateKey.speccedKey, cipherType.oaepParameterSpec)
        }

        return cipher.doFinal(toDecrypt.toByteArray()).toByteString().also {
            Timber.v("Decrypted %s... bytes to %s... bytes", toDecrypt.size, it.size)
        }
    }

    interface CipherProvider {
        fun createCipher(): Cipher
    }

    enum class CipherType(
        val transformation: String,
        val oaepParameterSpec: OAEPParameterSpec?
    ) {

        RSA_PKCS1_OAEP_PADDING(
            transformation = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING",
            oaepParameterSpec = OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
            )
        )
    }
}
