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

    fun decrypt(
        decryptionKey: ByteArray,
        encryptedData: ByteArray
    ): ByteArray {
        Security.addProvider(BouncyCastleProvider())
        val keySpec = SecretKeySpec(decode(decryptionKey), ALGORITHM)
        val input = decode(encryptedData)
        return with(Cipher.getInstance(TRANSFORMATION)) {
            init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)
            val output = ByteArray(getOutputSize(input.size))
            var outputLength = update(input, 0, input.size, output, 0)
            outputLength += doFinal(output, outputLength)
            output.copyOfRange(0, outputLength)
        }
    }
}

private const val ALGORITHM = "AES"
private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
