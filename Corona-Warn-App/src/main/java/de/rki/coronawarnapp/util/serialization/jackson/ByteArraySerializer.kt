package de.rki.coronawarnapp.util.serialization.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.addDeserializer
import com.fasterxml.jackson.module.kotlin.addSerializer
import com.google.gson.JsonParseException
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString

class ByteArraySerializer : JsonSerializer<ByteArray>() {
    override fun serialize(value: ByteArray?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.let { value.toByteString().base64().let { gen?.writeString(it) } }
    }
}

class ByteArrayDeserializer : JsonDeserializer<ByteArray>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ByteArray {
        requireNotNull(p) { "JsonParser is null" }
        val text = p.text
        return when {
            text.isEmpty() -> ByteArray(0)
            else -> {
                text.decodeBase64()?.toByteArray() ?: throw JsonParseException("Can't decode base64 ByteArray: $text")
            }
        }
    }
}

fun SimpleModule.registerByteArraySerialization() = apply {
    addSerializer(ByteArray::class, ByteArraySerializer())
    addDeserializer(ByteArray::class, ByteArrayDeserializer())
}
