package de.rki.coronawarnapp.util.serialization.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.joda.time.Duration
import org.json.JSONObject

class DurationAdapter : TypeAdapter<Duration>() {
    override fun write(out: JsonWriter, value: Duration?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.millis)
        }
    }

    override fun read(reader: JsonReader): Duration? = when (reader.peek()) {
        JSONObject.NULL -> {
            reader.nextNull()
            null
        }
        else -> {
            Duration.millis(reader.nextLong())
        }
    }
}
