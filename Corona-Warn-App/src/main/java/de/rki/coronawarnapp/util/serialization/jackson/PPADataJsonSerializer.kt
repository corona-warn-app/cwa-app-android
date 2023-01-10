package de.rki.coronawarnapp.util.serialization.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import java.io.IOException

class PPADataJsonSerializer : JsonSerializer<PpaData.PPADataAndroid?>() {
    @Throws(IOException::class)
    override fun serialize(value: PpaData.PPADataAndroid?, gen: JsonGenerator, serializers: SerializerProvider?) {
        if (value == null) {
            gen.writeNull()
        } else {
            value.toByteArray()?.toByteString()?.base64().let { gen.writeString(it) }
        }
    }
}

class PPADataJsonDeserializer : JsonDeserializer<PpaData.PPADataAndroid?>() {
    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): PpaData.PPADataAndroid? {
        return if (parser.currentToken == JsonToken.VALUE_NULL) {
            null
        } else {
            val raw = parser.text.decodeBase64()?.toByteArray()
            if (raw == null) {
                throw JsonParseException(parser, "Can't decode base64 ByteArray")
            } else {
                PpaData.PPADataAndroid.parseFrom(raw)
            }
        }
    }
}

fun SimpleModule.registerPPADataSerialization() = apply {
    addSerializer(PpaData.PPADataAndroid::class.java, PPADataJsonSerializer())
    addDeserializer(PpaData.PPADataAndroid::class.java, PPADataJsonDeserializer())
}
