package de.rki.coronawarnapp.util.serialization.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.Instant

class InstantAdapter : TypeAdapter<Instant>() {
    override fun write(out: JsonWriter, value: Instant?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toEpochMilli())
        }
    }

    override fun read(reader: JsonReader): Instant? = when (reader.peek()) {
        JsonToken.NULL -> {
            reader.nextNull()
            null
        }
        else -> {
            Instant.ofEpochMilli(reader.nextLong())
        }
    }
}
