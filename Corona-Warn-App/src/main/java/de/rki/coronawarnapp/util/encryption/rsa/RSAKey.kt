package de.rki.coronawarnapp.util.encryption.rsa

import de.rki.coronawarnapp.util.trimToLength
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.security.Key
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

interface RSAKey {
    val rawKey: ByteString
    val base64: String
        get() = rawKey.base64()

    val speccedKey: Key

    companion object {
        val KEY_FACTORY: KeyFactory = KeyFactory.getInstance("RSA")
    }

    data class Private(override val rawKey: ByteString) : RSAKey {

        constructor(key: PrivateKey) : this(key.encoded.toByteString())

        override val speccedKey: PrivateKey
            get() = KEY_FACTORY.generatePrivate(PKCS8EncodedKeySpec(rawKey.toByteArray()))

        override fun toString(): String = base64.trimToLength(16)
    }

    data class Public(override val rawKey: ByteString) : RSAKey {

        constructor(publicKey: PublicKey) : this(publicKey.encoded.toByteString())

        override val speccedKey: PublicKey
            get() = KEY_FACTORY.generatePublic(X509EncodedKeySpec(rawKey.toByteArray()))

        override fun toString(): String = base64.trimToLength(16)
    }
}
