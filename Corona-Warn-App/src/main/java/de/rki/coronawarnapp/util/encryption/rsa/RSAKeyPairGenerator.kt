package de.rki.coronawarnapp.util.encryption.rsa

import dagger.Reusable
import timber.log.Timber
import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.inject.Inject

@Reusable
class RSAKeyPairGenerator @Inject constructor() {

    fun generate(
        modolusLength: Int = DEFAULT_MODULUS_LENGTH
    ): RSAKeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(modolusLength)

        val keyPair = generator.genKeyPair()

        val pub = RSAKey.Public(keyPair.public)
        val priv = RSAKey.Private(keyPair.private)
        Timber.d("KeyPair generated: Pub=%s..., Priv=%s...", pub, priv)

        return RSAKeyPair(
            rawKeyPair = keyPair,
            publicKey = pub,
            privateKey = priv,
        )
    }

    data class RSAKeyPair(
        val rawKeyPair: KeyPair,
        val publicKey: RSAKey.Public,
        val privateKey: RSAKey.Private,
    )

    companion object {
        const val DEFAULT_MODULUS_LENGTH = 3072
    }
}
