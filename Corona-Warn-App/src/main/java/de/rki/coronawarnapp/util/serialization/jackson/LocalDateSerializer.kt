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
import java.time.LocalDate

class LocalDateJsonSerializer : JsonSerializer<LocalDate?>() {
    @Throws(IOException::class)
    override fun serialize(value: LocalDate?, gen: JsonGenerator, serializers: SerializerProvider?) {
        if (value == null) {
            gen.writeNull()
        } else {
            gen.writeString(value.toString())
        }
    }
}

class LocalDateJsonDeserializer : JsonDeserializer<LocalDate?>() {
    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): LocalDate? {
        return if (parser.currentToken === JsonToken.VALUE_NULL) {
            null
        } else LocalDate.parse(parser.text)
    }
}

fun SimpleModule.registerLocalDateSerialization() = apply {
    addSerializer(LocalDate::class.java, LocalDateJsonSerializer())
    addDeserializer(LocalDate::class.java, LocalDateJsonDeserializer())
}
