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
import okio.ByteString.Companion.EMPTY
import okio.ByteString.Companion.decodeBase64

class ByteStringSerializer : JsonSerializer<ByteString>() {
    override fun serialize(value: ByteString?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.let { gen?.writeString(it.base64()) } ?: gen?.writeNull()
    }
}

class ByteStringDeserializer : JsonDeserializer<ByteString>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ByteString {
        val text = p?.text
        return text?.decodeBase64() ?: EMPTY
    }
}

fun SimpleModule.registerByteStringSerialization() = apply {
    addSerializer(ByteString::class, ByteStringSerializer())
    addDeserializer(ByteString::class, ByteStringDeserializer())
}
