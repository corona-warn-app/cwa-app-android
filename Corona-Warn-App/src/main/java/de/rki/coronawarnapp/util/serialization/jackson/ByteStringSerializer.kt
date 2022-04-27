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
import okio.ByteString
import okio.ByteString.Companion.decodeBase64

class ByteStringSerializer : JsonSerializer<ByteString>() {
    override fun serialize(value: ByteString?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.let { gen?.writeString(it.base64()) }
    }
}

class ByteStringDeserializer : JsonDeserializer<ByteString>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ByteString {
        requireNotNull(p) { "JsonParser is null" }
        val text = p.text
        return when {
            text.isEmpty() -> ByteString.EMPTY
            else -> {
                val decoded = text.decodeBase64()
                checkNotNull(decoded) { "Cannot create ByteString from $text" }
            }
        }
    }
}

fun SimpleModule.registerByteStringSerialization() = apply {
    addSerializer(ByteString::class, ByteStringSerializer())
    addDeserializer(ByteString::class, ByteStringDeserializer())
}
