package de.rki.coronawarnapp.util.serialization.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import java.io.IOException
import java.time.Instant

class InstantJsonSerializer : JsonSerializer<Instant?>() {
    @Throws(IOException::class)
    override fun serialize(value: Instant?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
        } else {
            gen.writeNumber(value.toEpochMilli())
        }
    }
}

class InstantJsonDeserializer : JsonDeserializer<Instant?>() {
    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Instant? {
        return if (parser.currentToken == JsonToken.VALUE_NULL) {
            null
        } else Instant.ofEpochMilli(parser.longValue)
    }
}

fun SimpleModule.registerInstantSerialization() = apply {
    addSerializer(Instant::class.java, InstantJsonSerializer())
    addDeserializer(Instant::class.java, InstantJsonDeserializer())
}
