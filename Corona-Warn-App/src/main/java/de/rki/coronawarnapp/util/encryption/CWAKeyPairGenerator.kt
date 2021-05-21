package de.rki.coronawarnapp.util.encryption

import dagger.Reusable
import de.rki.coronawarnapp.util.trimToLength
import okio.ByteString
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.inject.Inject

@Reusable
class CWAKeyPairGenerator @Inject constructor() {

    fun generate(
        requirements: Requirements = RSA_DEFAULT
    ): CWAKeyPair {
        val generator = KeyPairGenerator.getInstance(requirements.algorithm.standardName)
        generator.initialize(requirements.modolusLength)

        val keyPair = generator.genKeyPair()

        val pub = PublicKey(keyPair.public.encoded.toByteString())
        val priv = PrivateKey(keyPair.private.encoded.toByteString())
        Timber.d(
            "KeyPair generated for %s: Pub=%s..., Priv=%s...",
            requirements,
            pub.base64.trimToLength(16),
            priv.base64.trimToLength(16)
        )

        return CWAKeyPair(
            requirements = requirements,
            rawKeyPair = keyPair,
            publicKey = pub,
            privateKey = priv,
        )
    }

    data class Requirements(
        val modolusLength: Int,
        val algorithm: Algorithm
    )

    data class CWAKeyPair(
        val requirements: Requirements,
        internal val rawKeyPair: KeyPair,
        val publicKey: PublicKey,
        val privateKey: PrivateKey,
    )

    interface Key {
        val key: ByteString
        val base64: String
            get() = key.base64()
    }

    @JvmInline
    value class PublicKey(override val key: ByteString) : Key

    @JvmInline
    value class PrivateKey(override val key: ByteString) : Key

    enum class Algorithm(val standardName: String) {
        RSA("RSA")
    }

    companion object {
        val RSA_DEFAULT = Requirements(
            modolusLength = 3072,
            algorithm = Algorithm.RSA
        )
    }
}
