package de.rki.coronawarnapp.util.encryption.rsa

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import de.rki.coronawarnapp.util.trimToLength
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
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

        class GsonAdapter : TypeAdapter<Private>() {
            override fun write(out: JsonWriter, value: Private?) {
                if (value == null) out.nullValue()
                else out.value(value.rawKey.base64())
            }

            override fun read(reader: JsonReader): Private? = when (reader.peek()) {
                JSONObject.NULL -> reader.nextNull().let { null }
                else -> Private(reader.nextString().decodeBase64()!!)
            }
        }
    }

    data class Public(override val rawKey: ByteString) : RSAKey {

        constructor(publicKey: PublicKey) : this(publicKey.encoded.toByteString())

        override val speccedKey: PublicKey
            get() = KEY_FACTORY.generatePublic(X509EncodedKeySpec(rawKey.toByteArray()))

        override fun toString(): String = base64.trimToLength(16)

        class GsonAdapter : TypeAdapter<Public>() {
            override fun write(out: JsonWriter, value: Public?) {
                if (value == null) out.nullValue()
                else out.value(value.rawKey.base64())
            }

            override fun read(reader: JsonReader): Public? = when (reader.peek()) {
                JSONObject.NULL -> reader.nextNull().let { null }
                else -> Public(reader.nextString().decodeBase64()!!)
            }
        }
    }
}
