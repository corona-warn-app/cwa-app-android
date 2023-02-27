package de.rki.coronawarnapp.util.serialization.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey.Public
import okio.ByteString.Companion.decodeBase64
import java.io.IOException

class PublicJsonSerializer : JsonSerializer<Public?>() {
    @Throws(IOException::class)
    override fun serialize(value: Public?, gen: JsonGenerator, serializers: SerializerProvider?) {
        if (value == null) {
            gen.writeNull()
        } else {
            gen.writeString(value.rawKey.base64())
        }
    }
}

class PublicJsonDeserializer : JsonDeserializer<Public?>() {
    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): Public? {
        return if (parser.currentToken == JsonToken.VALUE_NULL) {
            null
        } else Public(parser.text.decodeBase64()!!)
    }
}

fun SimpleModule.registerPublicSerialization() = apply {
    addSerializer(Public::class.java, PublicJsonSerializer())
    addDeserializer(Public::class.java, PublicJsonDeserializer())
}
