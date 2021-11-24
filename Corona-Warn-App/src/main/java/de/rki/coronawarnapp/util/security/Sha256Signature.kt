package de.rki.coronawarnapp.util.security

import de.rki.coronawarnapp.util.encoding.base64
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import javax.inject.Inject

class Sha256Signature @Inject constructor() {
    fun sign(data: ByteArray, privateKey: PrivateKey): String {
        with(Signature.getInstance(ALGORITHM)) {
            initSign(privateKey)
            update(data)
            return sign().base64()
        }
    }

    fun verify(data: ByteArray, publicKey: PublicKey, signature: ByteArray): Boolean {
        with(Signature.getInstance(ALGORITHM)) {
            initVerify(publicKey)
            update(data)
            return verify(signature)
        }
    }

    companion object {
        const val ALGORITHM = "SHA256withECDSA"
    }
}
