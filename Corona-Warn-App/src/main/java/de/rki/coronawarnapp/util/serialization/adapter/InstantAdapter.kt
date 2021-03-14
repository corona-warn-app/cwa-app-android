package de.rki.coronawarnapp.util.serialization.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.joda.time.Instant
import org.json.JSONObject

class InstantAdapter : TypeAdapter<Instant>() {
    override fun write(out: JsonWriter, value: Instant?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.millis)
        }
    }

    override fun read(reader: JsonReader): Instant? = when (reader.peek()) {
        JSONObject.NULL -> {
            reader.nextNull()
            null
        }
        else -> {
            Instant.ofEpochMilli(reader.nextLong())
        }
    }
}
