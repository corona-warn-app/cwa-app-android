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
import java.time.Duration

class DurationJsonSerializer : JsonSerializer<Duration?>() {
    @Throws(IOException::class)
    override fun serialize(value: Duration?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
        } else {
            gen.writeNumber(value.toMillis())
        }
    }
}

class DurationJsonDeserializer : JsonDeserializer<Duration?>() {
    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Duration? {
        return if (parser.currentToken == JsonToken.VALUE_NULL) {
            null
        } else Duration.ofMillis(parser.longValue)
    }
}

fun SimpleModule.registerDurationSerialization() = apply {
    addSerializer(Duration::class.java, DurationJsonSerializer())
    addDeserializer(Duration::class.java, DurationJsonDeserializer())
}
