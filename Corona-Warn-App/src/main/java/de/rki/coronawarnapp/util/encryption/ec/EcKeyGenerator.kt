package de.rki.coronawarnapp.util.encryption.ec

import dagger.Reusable
import de.rki.coronawarnapp.util.encoding.base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.inject.Inject

@Reusable
class EcKeyGenerator @Inject constructor() {

    fun generateECKeyPair(): ECKeyPair {
        val keyPair: KeyPair = KeyPairGenerator.getInstance(ALGORITHM)
            .run {
                initialize(KEY_SIZE)
                generateKeyPair()
            }
        return ECKeyPair(
            publicKey = keyPair.public,
            privateKey = keyPair.private,
            publicKeyBase64 = keyPair.public.encoded.base64()
        )
    }

    companion object {
        private const val KEY_SIZE = 256
        private const val ALGORITHM = "EC"
    }
}

data class ECKeyPair(
    /**
     * Native (EC) Public Key object
     */
    val publicKey: PublicKey,
    /**
     * Native (EC) Private Key object
     */
    val privateKey: PrivateKey,
    /**
     * A string that represents the base64-encoded public key (not byte sequence (Data / ByteArray))
     */
    val publicKeyBase64: String
)
